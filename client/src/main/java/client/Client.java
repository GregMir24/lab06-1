package client;

import common.*;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Scanner scanner;
    private boolean connected;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8888;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final int RECONNECT_DELAY = 2000;

    public void start() {
        scanner = new Scanner(System.in);

        // Подключение к серверу
        if (!connectToServer()) {
            System.out.println("Не удалось подключиться к серверу. Программа завершена.");
            return;
        }

        System.out.println("Подключение к серверу установлено. Введите 'help' для списка команд.");

        while (connected) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            if (command.equals("exit")) {
                break;
            }

            try {
                CommandRequest request = buildRequest(command, parts);
                if (request == null) {
                    continue;
                }

                out.writeObject(request);
                out.flush();

                CommandResponse response = (CommandResponse) in.readObject();

                if (response.isSuccess()) {
                    System.out.println(response.getMessage());
                    if (response.getVehicles() != null && !response.getVehicles().isEmpty()) {
                        response.getVehicles().forEach(System.out::println);
                    }
                } else {
                    System.err.println("Ошибка: " + response.getMessage());
                }

            } catch (SocketException e) {
                System.out.println("Соединение с сервером потеряно. Попытка переподключения...");
                reconnect();
            } catch (IOException e) {
                System.out.println("Ошибка связи с сервером: " + e.getMessage());
                reconnect();
            } catch (ClassNotFoundException e) {
                System.out.println("Ошибка при получении ответа от сервера");
            }
        }

        closeConnection();
        scanner.close();
        System.out.println("Клиент завершён");
    }

    private boolean connectToServer() {
        for (int attempt = 1; attempt <= MAX_RECONNECT_ATTEMPTS; attempt++) {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), 5000);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                connected = true;
                return true;
            } catch (IOException e) {
                System.out.println("Попытка подключения " + attempt + "/" + MAX_RECONNECT_ATTEMPTS + " не удалась");
                if (attempt < MAX_RECONNECT_ATTEMPTS) {
                    try {
                        Thread.sleep(RECONNECT_DELAY);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
        connected = false;
        return false;
    }

    private void reconnect() {
        closeConnection();
        connected = false;

        System.out.println("Переподключение к серверу...");
        if (connectToServer()) {
            System.out.println("Переподключение успешно!");
        } else {
            System.out.println("Не удалось переподключиться. Клиент завершён.");
        }
    }

    private void closeConnection() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {

        }
    }

    private CommandRequest buildRequest(String command, String[] parts) {
        switch (command) {
            case "help":
            case "info":
            case "show":
            case "clear":
                return new CommandRequest(command, new String[0]);

            case "insert":
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите ключ. Использование: insert <ключ>");
                    return null;
                }
                String key = parts[1];
                System.out.println("Введите данные нового элемента:");
                Vehicle vehicle = readVehicleFromConsole();
                if (vehicle == null) {
                    return null;
                }
                return new CommandRequest(command, key, vehicle);

            case "update":
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите ID. Использование: update <id>");
                    return null;
                }
                System.out.println("Введите новые данные:");
                Vehicle updatedVehicle = readVehicleFromConsole();
                if (updatedVehicle == null) {
                    return null;
                }
                updatedVehicle.setId(Integer.parseInt(parts[1]));
                return new CommandRequest(command, new String[]{parts[1]}, updatedVehicle);

            case "remove_key":
            case "remove_greater_key":
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите ключ");
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1]});

            case "replace_if_lowe":
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите ключ");
                    return null;
                }
                System.out.println("Введите новый объект для сравнения:");
                Vehicle newVehicle = readVehicleFromConsole();
                if (newVehicle == null) {
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1]}, newVehicle);

            case "count_less_than_type":
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите тип (PLANE, SUBMARINE, MOTORCYCLE)");
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1].toUpperCase()});

            case "filter_less_than_fuel_consumption":
                if (parts.length < 2) {
                    System.out.println("Ошибка: укажите число");
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1]});

            case "print_field_ascending_type":
                return new CommandRequest(command, new String[0]);

            default:
                System.out.println("Неизвестная команда: " + command);
                return null;
        }
    }

    private Vehicle readVehicleFromConsole() {
        Vehicle vehicle = new Vehicle();

        while (true) {
            System.out.print("Введите имя: ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                System.out.println("Имя не может быть пустым");
                continue;
            }
            try {
                vehicle.setName(name);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }

        while (true) {
            try {
                System.out.print("Введите X (целое число): ");
                long x = Long.parseLong(scanner.nextLine().trim());
                System.out.print("Введите Y (целое число): ");
                int y = Integer.parseInt(scanner.nextLine().trim());

                Coordinates coords = new Coordinates(x, y);
                vehicle.setCoordinates(coords);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }

        while (true) {
            try {
                System.out.print("Введите мощность (число > 0): ");
                float power = Float.parseFloat(scanner.nextLine().trim());
                if (power <= 0) {
                    System.out.println("Мощность должна быть больше 0");
                    continue;
                }
                vehicle.setEnginePower(power);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите число");
            }
        }

        while (true) {
            System.out.print("Введите расход топлива (число > 0, Enter для null): ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                vehicle.setFuelConsumption(null);
                break;
            }

            try {
                int fuel = Integer.parseInt(input);
                if (fuel <= 0) {
                    System.out.println("Расход должен быть больше 0");
                    continue;
                }
                vehicle.setFuelConsumption(fuel);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое число");
            }
        }

        while (true) {
            try {
                System.out.print("Введите тип машины (PLANE, SUBMARINE, MOTORCYCLE): ");
                String typeStr = scanner.nextLine().trim().toUpperCase();
                VehicleType type = VehicleType.valueOf(typeStr);
                vehicle.setType(type);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неверный тип. Доступны: PLANE, SUBMARINE, MOTORCYCLE");
            }
        }

        while (true) {
            try {
                System.out.print("Введите тип топлива (KEROSENE, DIESEL, NUCLEAR): ");
                String fuelTypeStr = scanner.nextLine().trim().toUpperCase();
                FuelType fuelType = FuelType.valueOf(fuelTypeStr);
                vehicle.setFuelType(fuelType);
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: неверный тип. Доступны: KEROSENE, DIESEL, NUCLEAR");
            }
        }

        return vehicle;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.start();
    }
}