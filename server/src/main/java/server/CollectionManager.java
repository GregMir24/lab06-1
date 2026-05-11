package server;

import common.*;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CollectionManager {
    private HashMap<String, Vehicle> collection;
    private ZonedDateTime initDate;

    public CollectionManager(HashMap<String, Vehicle> collection) {
        this.collection = collection;
        this.initDate = ZonedDateTime.now();
    }

    public HashMap<String, Vehicle> getCollection() {
        return collection;
    }

    public ZonedDateTime getInitDate() {
        return initDate;
    }

    public int size() {
        return collection.size();
    }

    public boolean isEmpty() {
        return collection.isEmpty();
    }

    public Vehicle getByKey(String key) {
        if (key == null) return null;
        return collection.get(key);
    }

    public void showAll() {
        if (collection.isEmpty()) {
            System.out.println("Коллекция пуста");
            return;
        }
        collection.values().stream()
                .sorted(Comparator.comparing(Vehicle::getId))
                .forEach(System.out::println);
    }

    public void info() {
        System.out.println("Тип коллекции: " + collection.getClass().getSimpleName());
        System.out.println("Дата инициализации: " + initDate);
        System.out.println("Количество элементов: " + collection.size());
        if (!collection.isEmpty()) {
            System.out.println("Ключи: " + collection.keySet().stream()
                    .collect(Collectors.joining(", ")));
        }
    }

    public void insert(String key, Vehicle vehicle) {
        if (key == null || vehicle == null) {
            throw new IllegalArgumentException("Ключ и объект не могут быть null");
        }
        if (collection.containsKey(key)) {
            throw new IllegalArgumentException("Ключ '" + key + "' уже существует");
        }
        collection.put(key, vehicle);
        System.out.println("Элемент с ключом '" + key + "' добавлен");
    }

    public void remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Ключ не может быть null");
        }
        Vehicle removed = collection.remove(key);
        if (removed == null) {
            throw new IllegalArgumentException("Ключ '" + key + "' не найден");
        }
        System.out.println("Элемент с ключом '" + key + "' удалён");
    }

    public void clear() {
        collection.clear();
        System.out.println("Коллекция очищена");
    }

    public void removeGreaterKey(String keyThreshold) {
        if (keyThreshold == null) {
            throw new IllegalArgumentException("Ключ не может быть null");
        }
        List<String> toRemove = collection.keySet().stream()
                .filter(key -> key.compareTo(keyThreshold) > 0)
                .collect(Collectors.toList());
        toRemove.forEach(collection::remove);
        System.out.println("Удалено элементов: " + toRemove.size());
    }

    public int countLessThanType(VehicleType type) {
        if (type == null) {
            throw new IllegalArgumentException("Тип не может быть null");
        }
        return (int) collection.values().stream()
                .filter(v -> v.getType() != null)
                .filter(v -> v.getType().ordinal() < type.ordinal())
                .count();
    }

    public void filterLessThanFuelConsumption(float threshold) {
        List<Vehicle> result = collection.values().stream()
                .filter(v -> v.getFuelConsumption() != null)
                .filter(v -> v.getFuelConsumption() < threshold)
                .collect(Collectors.toList());

        if (result.isEmpty()) {
            System.out.println("Нет элементов с расходом < " + threshold);
        } else {
            System.out.println("Элементы с расходом < " + threshold + ":");
            result.forEach(System.out::println);
        }
    }

    public void printFieldAscendingType() {
        List<VehicleType> types = collection.values().stream()
                .map(Vehicle::getType)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .collect(Collectors.toList());

        if (types.isEmpty()) {
            System.out.println("Нет элементов с типами");
        } else {
            System.out.println("Типы в порядке возрастания:");
            types.forEach(System.out::println);
        }
    }

    public void update(int id, Vehicle newVehicle) {
        if (newVehicle == null) {
            throw new IllegalArgumentException("Vehicle не может быть null");
        }

        String foundKey = collection.entrySet().stream()
                .filter(entry -> entry.getValue().getId() == id)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);

        if (foundKey == null) {
            throw new IllegalArgumentException("Элемент с id " + id + " не найден");
        }

        newVehicle.setId(id);
        collection.put(foundKey, newVehicle);
        System.out.println("Элемент с id " + id + " обновлён");
    }

    public void replace(String key, Vehicle newVehicle) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Ключ не может быть пустым");
        }
        if (newVehicle == null) {
            throw new IllegalArgumentException("Vehicle не может быть null");
        }
        if (!collection.containsKey(key)) {
            throw new IllegalArgumentException("Ключ '" + key + "' не найден");
        }
        collection.put(key, newVehicle);
        System.out.println("Элемент с ключом '" + key + "' заменён");
    }

    public void saveToFile(String filename, XmlParser parser) {
        if (filename == null || filename.trim().isEmpty()) {
            System.out.println("Ошибка: не указано имя файла");
            return;
        }

        if (collection.isEmpty()) {
            System.out.println("Коллекция пуста, сохранение не требуется");
            return;
        }

        try {
            VehicleCollection saveCollection = new VehicleCollection(this.collection);
            parser.saveObj(new File(filename), saveCollection);
            System.out.println("Коллекция сохранена в файл: " + filename);
        } catch (Exception e) {
            System.out.println("Ошибка при сохранении: " + e.getMessage());
        }
    }
}