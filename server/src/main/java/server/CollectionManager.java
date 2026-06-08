package server;

import common.*;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class CollectionManager {
    private Map<String, Vehicle> collection;
    private ZonedDateTime initDate;
    private DatabaseManager dbManager;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CollectionManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.collection = Collections.synchronizedMap(new HashMap<>());
        this.initDate = ZonedDateTime.now();
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        HashMap<String, Vehicle> loaded = dbManager.loadAllVehicles();
        lock.writeLock().lock();
        try {
            collection.putAll(loaded);
        } finally {
            lock.writeLock().unlock();
        }
        System.out.println("Загружено " + collection.size() + " элементов из БД");
    }

    public Map<String, Vehicle> getCollection() {
        lock.readLock().lock();
        try {
            return new HashMap<>(collection);
        } finally {
            lock.readLock().unlock();
        }
    }

    public ZonedDateTime getInitDate() {
        return initDate;
    }

    public int size() {
        lock.readLock().lock();
        try {
            return collection.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return collection.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    public Vehicle getByKey(String key) {
        lock.readLock().lock();
        try {
            return collection.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public String showAll() {
        lock.readLock().lock();
        try {
            if (collection.isEmpty()) {
                return "Коллекция пуста";
            }
            return collection.values().stream()
                    .sorted(Comparator.comparing(Vehicle::getId))
                    .map(Vehicle::toString)
                    .collect(Collectors.joining("\n"));
        } finally {
            lock.readLock().unlock();
        }
    }

    public String info() {
        lock.readLock().lock();
        try {
            return String.format("Тип коллекции: %s\nДата инициализации: %s\nКоличество элементов: %d",
                    collection.getClass().getSimpleName(), initDate, collection.size());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean insert(String key, Vehicle vehicle, String owner) {
        int id = dbManager.insertVehicle(vehicle, owner);
        if (id > 0) {
            lock.writeLock().lock();
            try {
                collection.put(String.valueOf(id), vehicle);
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        }
        return false;
    }

    public boolean update(int id, Vehicle newVehicle, String owner) {
        if (dbManager.updateVehicle(id, newVehicle, owner)) {
            lock.writeLock().lock();
            try {
                collection.put(String.valueOf(id), newVehicle);
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        }
        return false;
    }

    public boolean remove(String key, String owner) {
        if (dbManager.deleteVehicle(key, owner)) {
            lock.writeLock().lock();
            try {
                collection.remove(key);
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        }
        return false;
    }

    public void clear(String owner) {
        dbManager.clearCollection(owner);
        lock.writeLock().lock();
        try {
            collection.entrySet().removeIf(entry -> entry.getValue().getOwner().equals(owner));
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean removeGreaterKey(String keyThreshold, String owner) {
        if (dbManager.deleteGreaterKeys(keyThreshold, owner)) {
            lock.writeLock().lock();
            try {
                int threshold = Integer.parseInt(keyThreshold);
                collection.entrySet().removeIf(entry -> {
                    int id = Integer.parseInt(entry.getKey());
                    return id > threshold && entry.getValue().getOwner().equals(owner);
                });
            } finally {
                lock.writeLock().unlock();
            }
            return true;
        }
        return false;
    }

    public int countLessThanType(VehicleType type) {
        lock.readLock().lock();
        try {
            return (int) collection.values().stream()
                    .filter(v -> v.getType() != null)
                    .filter(v -> v.getType().ordinal() < type.ordinal())
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Vehicle> filterLessThanFuelConsumption(float threshold) {
        lock.readLock().lock();
        try {
            return collection.values().stream()
                    .filter(v -> v.getFuelConsumption() != null)
                    .filter(v -> v.getFuelConsumption() < threshold)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<VehicleType> getSortedTypes() {
        lock.readLock().lock();
        try {
            return collection.values().stream()
                    .map(Vehicle::getType)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted(Comparator.comparingInt(Enum::ordinal))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean replaceIfLower(String key, Vehicle newVehicle, String owner) {
        lock.writeLock().lock();
        try {
            Vehicle oldVehicle = collection.get(key);
            if (oldVehicle == null) {
                return false;
            }
            if (!oldVehicle.getOwner().equals(owner)) {
                return false;
            }
            if (newVehicle.compareTo(oldVehicle) < 0) {
                int id = Integer.parseInt(key);
                lock.writeLock().unlock(); // Временный unlock для вызова update
                boolean result = update(id, newVehicle, owner);
                lock.writeLock().lock();
                return result;
            }
            return false;
        } finally {
            if (lock.isWriteLockedByCurrentThread()) {
                lock.writeLock().unlock();
            }
        }
    }

    public boolean checkOwnership(String key, String owner) {
        lock.readLock().lock();
        try {
            Vehicle vehicle = collection.get(key);
            if (vehicle == null) return false;
            return vehicle.getOwner().equals(owner);
        } finally {
            lock.readLock().unlock();
        }
    }
}