package common;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;

    @Override
    public ZonedDateTime unmarshal(String v) throws Exception {
        if (v == null || v.trim().isEmpty()) {
            return ZonedDateTime.now();
        }

        try {
            return ZonedDateTime.parse(v.trim(), FORMATTER);
        } catch (DateTimeParseException e) {
            System.err.println("Ошибка парсинга даты '" + v + "', используется текущая");
            return ZonedDateTime.now();
        }
    }

    @Override
    public String marshal(ZonedDateTime v) throws Exception {
        if (v == null) {
            return ZonedDateTime.now().format(FORMATTER);
        }
        return v.format(FORMATTER);
    }
}