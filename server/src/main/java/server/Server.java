package server;

import common.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class Server {
    private CollectionManager manager;
    private XmlParser xmlParser;
    private String filename;
    private boolean running;

    public Server(String filename) {
        this.filename = filename;
        this.xmlParser = new XmlParser();
        this.running = true;

        File file = new File(filename);
        if (file.exists()) {
            Vehicle.clearIds();
            VehicleCollection loaded = (VehicleCollection) xmlParser.loadObj(file, VehicleCollection.class);
            if (loaded != null) {
                HashMap<String, Vehicle> loadedMap = loaded.toHashMap();
                for (Vehicle v : loadedMap.values()) {
                    if (v.getId() != null) {
                        Vehicle.getIds().add(v.getId());
                    }
                }
                manager = new CollectionManager(loadedMap);
                System.out.println("Коллекция загружена из файла: " + filename);
            } else {
                manager = new CollectionManager(new HashMap<>());
                System.out.println("Создана новая пустая коллекция");
            }
        } else {
            manager = new CollectionManager(new HashMap<>());
            System.out.println("Файл не найден, создана пустая коллекция");
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Сохранение коллекции перед завершением...");
            manager.saveToFile(filename, xmlParser);
        }));
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            System.out.println("Сервер запущен на порту 8080");
            System.out.println("Однопоточный режим. Ожидание подключений...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Клиент подключился: " + clientSocket.getInetAddress());
                handleClient(clientSocket);
                clientSocket.close();
                System.out.println("Клиент отключился");
            }
        } catch (IOException e) {
            System.err.println("Ошибка сервера: " + e.getMessage());
        }
    }

    private void handleClient(Socket clientSocket) {
        try (ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream())) {

            CommandRequest request = (CommandRequest) in.readObject();
            System.out.println("Получена команда: " + request.getCommandName());

            CommandResponse response = processCommand(request);

            out.writeObject(response);
            out.flush();
            System.out.println("Ответ отправлен клиенту");

        } catch (IOException e) {
            System.err.println("Ошибка ввода-вывода: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("Ошибка: неизвестный тип запроса");
        } catch (Exception e) {
            System.err.println("Ошибка обработки клиента: " + e.getMessage());
        }
    }

    private CommandResponse processCommand(CommandRequest request) {
        String command = request.getCommandName();
        String[] args = request.getArgs();

        switch (command) {
            case "help":
                return helpCommand();
            case "info":
                return infoCommand();
            case "show":
                return showCommand();
            case "insert":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указан ключ");
                }
                if (request.getVehicle() == null) {
                    return new CommandResponse(false, "Ошибка: не указан объект Vehicle");
                }
                return insertCommand(args[0], request.getVehicle());
            case "update":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указан ID");
                }
                if (request.getVehicle() == null) {
                    return new CommandResponse(false, "Ошибка: не указан объект Vehicle");
                }
                try {
                    int id = Integer.parseInt(args[0]);
                    return updateCommand(id, request.getVehicle());
                } catch (NumberFormatException e) {
                    return new CommandResponse(false, "Ошибка: ID должен быть числом");
                }
            case "remove_key":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указан ключ");
                }
                return removeKeyCommand(args[0]);
            case "clear":
                return clearCommand();
            case "remove_greater_key":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указан ключ");
                }
                return removeGreaterKeyCommand(args[0]);
            case "count_less_than_type":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указан тип");
                }
                try {
                    VehicleType type = VehicleType.valueOf(args[0].toUpperCase());
                    return countLessThanTypeCommand(type);
                } catch (IllegalArgumentException e) {
                    return new CommandResponse(false, "Ошибка: неверный тип. Доступны: PLANE, SUBMARINE, MOTORCYCLE");
                }
            case "filter_less_than_fuel_consumption":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указано значение");
                }
                try {
                    float threshold = Float.parseFloat(args[0]);
                    return filterLessThanFuelConsumptionCommand(threshold);
                } catch (NumberFormatException e) {
                    return new CommandResponse(false, "Ошибка: введите число");
                }
            case "print_field_ascending_type":
                return printFieldAscendingTypeCommand();
            case "replace_if_lowe":
                if (args.length < 1) {
                    return new CommandResponse(false, "Ошибка: не указан ключ");
                }
                if (request.getVehicle() == null) {
                    return new CommandResponse(false, "Ошибка: не указан объект Vehicle");
                }
                return replaceIfLowerCommand(args[0], request.getVehicle());
            default:
                return new CommandResponse(false, "Неизвестная команда: " + command);
        }
    }

    private CommandResponse helpCommand() {
        String help = "===== Доступные команды =====\n" +
                "help - справка\n" +
                "info - информация о коллекции\n" +
                "show - показать все элементы\n" +
                "insert <ключ> - добавить элемент\n" +
                "update <id> - обновить элемент\n" +
                "remove_key <ключ> - удалить элемент по ключу\n" +
                "clear - очистить коллекцию\n" +
                "remove_greater_key <ключ> - удалить ключи больше заданного\n" +
                "count_less_than_type <тип> - количество элементов с типом меньше\n" +
                "filter_less_than_fuel_consumption <число> - фильтр по расходу\n" +
                "print_field_ascending_type - вывести типы по возрастанию\n" +
                "replace_if_lowe <ключ> - заменить если новое значение меньше\n" +
                "exit - выход из клиента\n" +
                "==============================";
        return new CommandResponse(true, help);
    }

    private CommandResponse infoCommand() {
        String info = "=== Информация о коллекции ===\n" +
                "Тип: " + manager.getCollection().getClass().getSimpleName() + "\n" +
                "Размер: " + manager.size() + "\n" +
                "Дата инициализации: " + manager.getInitDate();
        return new CommandResponse(true, info);
    }

    private CommandResponse showCommand() {
        if (manager.isEmpty()) {
            return new CommandResponse(true, "Коллекция пуста");
        }

        List<Vehicle> sorted = manager.getCollection().values().stream()
                .sorted((v1, v2) -> {
                    long x1 = v1.getCoordinates().getX();
                    long x2 = v2.getCoordinates().getX();
                    if (x1 != x2) {
                        return Long.compare(x1, x2);
                    }
                    return Integer.compare(v1.getCoordinates().getY(), v2.getCoordinates().getY());
                })
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("=== Элементы коллекции (сортировка по координатам) ===\n");
        for (Vehicle v : sorted) {
            sb.append(v).append("\n");
        }
        return new CommandResponse(true, sb.toString(), sorted);
    }

    private CommandResponse insertCommand(String key, Vehicle vehicle) {
        try {
            manager.insert(key, vehicle);
            return new CommandResponse(true, "Элемент добавлен. Ключ: " + key + ", ID: " + vehicle.getId());
        } catch (IllegalArgumentException e) {
            return new CommandResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    private CommandResponse updateCommand(int id, Vehicle newVehicle) {
        try {
            manager.update(id, newVehicle);
            return new CommandResponse(true, "Элемент с id " + id + " обновлён");
        } catch (IllegalArgumentException e) {
            return new CommandResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    private CommandResponse removeKeyCommand(String key) {
        try {
            manager.remove(key);
            return new CommandResponse(true, "Элемент с ключом '" + key + "' удалён");
        } catch (IllegalArgumentException e) {
            return new CommandResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    private CommandResponse clearCommand() {
        manager.clear();
        return new CommandResponse(true, "Коллекция очищена");
    }

    private CommandResponse removeGreaterKeyCommand(String keyThreshold) {
        try {
            manager.removeGreaterKey(keyThreshold);
            return new CommandResponse(true, "Удалены элементы с ключом > '" + keyThreshold + "'");
        } catch (IllegalArgumentException e) {
            return new CommandResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    private CommandResponse countLessThanTypeCommand(VehicleType type) {
        long count = manager.getCollection().values().stream()
                .filter(v -> v.getType() != null)
                .filter(v -> v.getType().ordinal() < type.ordinal())
                .count();
        return new CommandResponse(true, "Количество элементов с типом < " + type + ": " + count);
    }

    private CommandResponse filterLessThanFuelConsumptionCommand(float threshold) {
        List<Vehicle> filtered = manager.getCollection().values().stream()
                .filter(v -> v.getFuelConsumption() != null)
                .filter(v -> v.getFuelConsumption() < threshold)
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new CommandResponse(true, "Нет элементов с расходом < " + threshold);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Элементы с расходом < ").append(threshold).append(":\n");
        filtered.forEach(v -> sb.append(v).append("\n"));
        return new CommandResponse(true, sb.toString(), filtered);
    }

    private CommandResponse printFieldAscendingTypeCommand() {
        List<VehicleType> types = manager.getCollection().values().stream()
                .map(Vehicle::getType)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .collect(Collectors.toList());

        if (types.isEmpty()) {
            return new CommandResponse(true, "Нет элементов с типами");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Типы в порядке возрастания:\n");
        types.forEach(t -> sb.append(t).append("\n"));
        return new CommandResponse(true, sb.toString());
    }

    private CommandResponse replaceIfLowerCommand(String key, Vehicle newVehicle) {
        try {
            Vehicle oldVehicle = manager.getByKey(key);
            if (oldVehicle == null) {
                return new CommandResponse(false, "Ключ '" + key + "' не найден");
            }
            if (newVehicle.compareTo(oldVehicle) < 0) {
                manager.replace(key, newVehicle);
                return new CommandResponse(true, "Значение заменено (новое меньше старого)");
            } else {
                return new CommandResponse(true, "Замена не выполнена (новое не меньше старого)");
            }
        } catch (IllegalArgumentException e) {
            return new CommandResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    public void stop() {
        running = false;
    }

    public static void main(String[] args) {
        String filename = System.getenv("VEHICLE_DATA_FILE");
        if (filename == null || filename.trim().isEmpty()) {
            filename = "vehicles.xml";
            System.out.println("Переменная VEHICLE_DATA_FILE не задана, используется: " + filename);
        }

        Server server = new Server(filename);
        server.start();
    }
}