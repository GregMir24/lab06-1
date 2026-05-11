package common;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String commandName;
    private String[] args;
    private Vehicle vehicle;

    public CommandRequest(String commandName, String[] args) {
        this.commandName = commandName;
        this.args = args;
        this.vehicle = null;
    }

    public CommandRequest(String commandName, String key, Vehicle vehicle) {
        this.commandName = commandName;
        this.args = new String[]{key};
        this.vehicle = vehicle;
    }

    public CommandRequest(String commandName, String[] args, Vehicle vehicle) {
        this.commandName = commandName;
        this.args = args;
        this.vehicle = vehicle;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }
}