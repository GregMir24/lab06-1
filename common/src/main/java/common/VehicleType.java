package common;

import java.io.Serializable;

public enum VehicleType implements Serializable {
    PLANE,
    SUBMARINE,
    MOTORCYCLE;

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