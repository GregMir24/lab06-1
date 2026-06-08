package common;

import java.io.Serializable;
import java.util.List;

public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private List<Vehicle> vehicles;

    public CommandResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.vehicles = null;
    }

    public CommandResponse(boolean success, String message, List<Vehicle> vehicles) {
        this.success = success;
        this.message = message;
        this.vehicles = vehicles;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }
}