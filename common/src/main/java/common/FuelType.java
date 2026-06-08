package common;

import java.io.Serializable;

public enum FuelType implements Serializable {
    KEROSENE,
    DIESEL,
    NUCLEAR;

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