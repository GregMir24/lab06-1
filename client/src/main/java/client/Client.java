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
    private User currentUser;

    public void start() {
        scanner = new Scanner(System.in);

        try {
            socket = new Socket("localhost", 8787);
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Подключен к серверу");

            boolean authenticated = false;
            while (!authenticated) {
                System.out.print("Введите login или register: ");
                String input = scanner.nextLine().trim();

                out.writeObject(new CommandRequest(input, new String[0]));
                out.flush();

                CommandResponse response = (CommandResponse) in.readObject();

                if (response.getMessage().equals("Введите login или register:")) {
                    response = (CommandResponse) in.readObject();
                }

                if ("register".equals(input) || "login".equals(input)) {
                    System.out.print("Логин: ");
                    String login = scanner.nextLine();
                    out.writeObject(new CommandRequest(login, new String[0]));
                    out.flush();

                    response = (CommandResponse) in.readObject();
                    System.out.print("Пароль: ");
                    String pass = scanner.nextLine();
                    out.writeObject(new CommandRequest(pass, new String[0]));
                    out.flush();

                    response = (CommandResponse) in.readObject();
                    System.out.println(response.getMessage());

                    if (response.isSuccess()) {
                        currentUser = new User(login, pass);
                        currentUser.setAuthenticated(true);
                        response = (CommandResponse) in.readObject();
                        System.out.println(response.getMessage());
                        authenticated = true;
                        break;
                    } else {
                        System.out.println("Повторите попытку.");
                        System.out.println("-----------------------------------");
                    }
                }
            }
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.equals("exit")) break;

                CommandRequest request = buildRequest(input);
                if (request == null) continue;
                request.setUser(currentUser);

                out.writeObject(request);
                out.flush();

                CommandResponse resp = (CommandResponse) in.readObject();
                System.out.println(resp.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { socket.close(); } catch (IOException e) {}
            scanner.close();
        }
    }

    private CommandRequest buildRequest(String input) {
        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "help":
            case "info":
            case "show":
            case "clear":
            case "print_field_ascending_type":
                return new CommandRequest(command, new String[0]);

            case "insert":
                if (parts.length < 2) {
                    System.out.println("Нужен ключ");
                    return null;
                }
                System.out.println("Введите данные:");
                Vehicle v = readVehicle();
                return v == null ? null : new CommandRequest("insert", parts[1], v);

            case "update":
                if (parts.length < 2) {
                    System.out.println("Нужен ID");
                    return null;
                }
                System.out.println("Введите данные:");
                Vehicle v2 = readVehicle();
                return v2 == null ? null : new CommandRequest("update", new String[]{parts[1]}, v2);

            case "remove_key":
            case "remove_greater_key":
                if (parts.length < 2) {
                    System.out.println("Нужен ключ");
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1]});

            case "replace_if_lowe":
                if (parts.length < 2) {
                    System.out.println("Нужен ключ");
                    return null;
                }
                System.out.println("Введите данные:");
                Vehicle v3 = readVehicle();
                return v3 == null ? null : new CommandRequest(command, new String[]{parts[1]}, v3);

            case "count_less_than_type":
                if (parts.length < 2) {
                    System.out.println("Нужен тип");
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1].toUpperCase()});

            case "filter_less_than_fuel_consumption":
                if (parts.length < 2) {
                    System.out.println("Нужно число");
                    return null;
                }
                return new CommandRequest(command, new String[]{parts[1]});

            default:
                System.out.println("Неизвестная команда: " + command);
                return null;
        }
    }

    private Vehicle readVehicle() {
        Vehicle v = new Vehicle();
        try {
            System.out.print("Имя: ");
            v.setName(scanner.nextLine().trim());
            System.out.print("X: ");
            long x = Long.parseLong(scanner.nextLine().trim());
            System.out.print("Y: ");
            int y = Integer.parseInt(scanner.nextLine().trim());
            v.setCoordinates(new Coordinates(x, y));
            System.out.print("Мощность (>0): ");
            v.setEnginePower(Float.parseFloat(scanner.nextLine().trim()));
            System.out.print("Расход (Enter для null): ");
            String fuel = scanner.nextLine().trim();
            if (!fuel.isEmpty()) v.setFuelConsumption(Integer.parseInt(fuel));
            System.out.print("Тип (PLANE, SUBMARINE, MOTORCYCLE): ");
            v.setType(VehicleType.valueOf(scanner.nextLine().trim().toUpperCase()));
            System.out.print("Топливо (KEROSENE, DIESEL, NUCLEAR): ");
            v.setFuelType(FuelType.valueOf(scanner.nextLine().trim().toUpperCase()));
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            return null;
        }
        return v;
    }

    public static void main(String[] args) {
        new Client().start();
    }
}