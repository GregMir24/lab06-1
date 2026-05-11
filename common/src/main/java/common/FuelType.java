package common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.io.Serializable;

@XmlEnum
public enum FuelType implements Serializable {
    @XmlEnumValue("kerosene") KEROSENE,
    @XmlEnumValue("diesel") DIESEL,
    @XmlEnumValue("nuclear") NUCLEAR;

    public static FuelType fromString(String value) {
        for (FuelType type : FuelType.values()) {
            if (type.name().equalsIgnoreCase(value) ||
                    type.toString().equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}