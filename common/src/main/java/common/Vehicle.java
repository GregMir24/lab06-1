package common;

import java.io.Serializable;
import java.time.ZonedDateTime;

public class Vehicle implements Comparable<Vehicle>, Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private Coordinates coordinates;
    private ZonedDateTime creationDate;
    private float enginePower;
    private Integer fuelConsumption;
    private VehicleType type;
    private FuelType fuelType;
    private String owner;

    public Vehicle() {
        this.creationDate = ZonedDateTime.now();
    }

    public Vehicle(String name, Coordinates coordinates, float enginePower,
                   Integer fuelConsumption, VehicleType type, FuelType fuelType, String owner) {
        this.creationDate = ZonedDateTime.now();
        this.name = name;
        this.coordinates = coordinates;
        this.enginePower = enginePower;
        this.fuelConsumption = fuelConsumption;
        this.type = type;
        this.fuelType = fuelType;
        this.owner = owner;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        this.name = name;
    }

    public void setCoordinates(Coordinates coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Координаты не могут быть null");
        }
        this.coordinates = coordinates;
    }

    public void setEnginePower(float enginePower) {
        if (enginePower <= 0) {
            throw new IllegalArgumentException("Мощность должна быть больше 0");
        }
        this.enginePower = enginePower;
    }

    public void setFuelConsumption(Integer fuelConsumption) {
        if (fuelConsumption != null && fuelConsumption <= 0) {
            throw new IllegalArgumentException("Расход топлива должен быть > 0");
        }
        this.fuelConsumption = fuelConsumption;
    }

    public void setType(VehicleType type) {
        this.type = type;
    }

    public void setFuelType(FuelType fuelType) {
        if (fuelType == null) {
            throw new IllegalArgumentException("Тип топлива не может быть null");
        }
        this.fuelType = fuelType;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public float getEnginePower() {
        return enginePower;
    }

    public Integer getFuelConsumption() {
        return fuelConsumption;
    }

    public VehicleType getType() {
        return type;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public ZonedDateTime getCreationDateObject() {
        return creationDate;
    }

    public String getOwner() {
        return owner;
    }

    @Override
    public int compareTo(Vehicle other) {
        if (other == null) return 1;
        return Integer.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vehicle vehicle = (Vehicle) obj;
        return id.equals(vehicle.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("Vehicle{id=%d, name='%s', owner='%s', coords=(%d,%d), engine=%.1f, fuel=%s, type=%s}",
                id, name, owner,
                coordinates != null ? coordinates.getX() : 0,
                coordinates != null ? coordinates.getY() : 0,
                enginePower,
                fuelType,
                type);
    }

    public Vehicle copy() {
        Vehicle copy = new Vehicle();
        copy.id = this.id;
        copy.name = this.name;
        if (this.coordinates != null) {
            copy.coordinates = this.coordinates.copy();
        }
        copy.creationDate = this.creationDate;
        copy.enginePower = this.enginePower;
        copy.fuelConsumption = this.fuelConsumption;
        copy.type = this.type;
        copy.fuelType = this.fuelType;
        copy.owner = this.owner;
        return copy;
    }
}