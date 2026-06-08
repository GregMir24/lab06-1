package common;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String commandName;
    private String[] args;
    private Vehicle vehicle;
    private User user;

    public CommandRequest(String commandName, String[] args) {
        this.commandName = commandName;
        this.args = args;
        this.vehicle = null;
        this.user = null;
    }

    public CommandRequest(String commandName, String key, Vehicle vehicle) {
        this.commandName = commandName;
        this.args = new String[]{key};
        this.vehicle = vehicle;
        this.user = null;
    }

    public CommandRequest(String commandName, String[] args, Vehicle vehicle) {
        this.commandName = commandName;
        this.args = args;
        this.vehicle = vehicle;
        this.user = null;
    }

    public CommandRequest(String commandName, String[] args, User user) {
        this.commandName = commandName;
        this.args = args;
        this.vehicle = null;
        this.user = user;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}