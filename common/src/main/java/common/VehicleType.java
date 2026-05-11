package common;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import java.io.Serializable;

@XmlEnum
public enum VehicleType implements Serializable {
    @XmlEnumValue("plane") PLANE,
    @XmlEnumValue("submarine") SUBMARINE,
    @XmlEnumValue("motorcycle") MOTORCYCLE;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static VehicleType fromString(String text) {
        for (VehicleType type : VehicleType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }
}