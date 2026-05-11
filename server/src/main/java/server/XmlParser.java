package server;

import common.VehicleCollection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class XmlParser {

    public void saveObj(File file, Object object) {
        if (file == null) {
            System.out.println("Ошибка: не указан файл для сохранения");
            return;
        }
        if (object == null) {
            System.out.println("Ошибка: нечего сохранять");
            return;
        }

        if (object instanceof VehicleCollection) {
            VehicleCollection collection = (VehicleCollection) object;
            if (collection.getEntries() == null || collection.getEntries().isEmpty()) {
                System.out.println("Коллекция пуста, сохранение не требуется");
                return;
            }
        }

        String path = file.getPath();
        if (path == null || path.trim().isEmpty()) {
            System.out.println("Ошибка: не указано имя файла");
            return;
        }

        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                System.out.println("Ошибка: не удалось создать директорию " + parentDir);
                return;
            }
        }

        if (file.exists() && !file.canWrite()) {
            System.out.println("Ошибка: нет прав на запись в файл " + file.getName());
            return;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8)) {

            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.marshal(object, writer);

            System.out.println("Сохранено в файл: " + file.getName());

        } catch (JAXBException e) {
            System.out.println("Ошибка JAXB при сохранении: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.out.println("Ошибка: не удалось создать файл " + file.getName());
        } catch (IOException e) {
            System.out.println("Ошибка ввода-вывода: " + e.getMessage());
        }
    }

    public Object loadObj(File file, Class<?> clazz) {
        if (file == null) {
            System.out.println("Ошибка: не указан файл для загрузки");
            return null;
        }

        if (!file.exists()) {
            System.out.println("Файл не найден: " + file.getName());
            return null;
        }

        if (!file.canRead()) {
            System.out.println("Ошибка: нет прав на чтение файла " + file.getName());
            return null;
        }

        if (file.length() == 0) {
            System.out.println("Файл пустой: " + file.getName());
            return null;
        }

        // Проверка, что файл является XML
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
            bis.mark(100);
            byte[] header = new byte[100];
            int bytesRead = bis.read(header);
            bis.reset();

            String headerStr = new String(header, 0, bytesRead, StandardCharsets.UTF_8);
            if (!headerStr.contains("<?xml") && !headerStr.contains("<")) {
                System.out.println("Ошибка: файл " + file.getName() + " не является XML");
                return null;
            }
        } catch (IOException e) {
            System.out.println("Ошибка при проверке файла");
            return null;
        }

        try (FileInputStream fis = new FileInputStream(file);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Object result = unmarshaller.unmarshal(bis);

            if (result == null) {
                System.out.println("Не удалось прочитать данные из файла");
                return null;
            }

            System.out.println("Данные загружены из файла: " + file.getName());
            return result;

        } catch (JAXBException e) {
            System.out.println("Ошибка: файл " + file.getName() + " повреждён или имеет неверный формат");
            return null;
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден: " + file.getName());
            return null;
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла");
            return null;
        }
    }
}