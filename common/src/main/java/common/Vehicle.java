package common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@XmlRootElement
@XmlType(propOrder = {"id", "name", "coordinates", "creationDateObject", "enginePower",
        "fuelConsumption", "type", "fuelType"})
public class Vehicle implements Comparable<Vehicle>, Serializable {
    private static final long serialVersionUID = 1L;

    private static Set<Integer> ids = new HashSet<>();
    private Integer id;
    private String name;
    private Coordinates coordinates;
    private java.time.ZonedDateTime creationDate;
    private float enginePower;
    private Integer fuelConsumption;
    private VehicleType type;
    private FuelType fuelType;

    public Vehicle() {
        this.id = generateId();
        this.creationDate = ZonedDateTime.now();
    }

    public Vehicle(String name, Coordinates coordinates, float enginePower,
                   Integer fuelConsumption, VehicleType type, FuelType fuelType) {
        this.id = generateId();
        this.creationDate = ZonedDateTime.now();
        this.name = name;
        this.coordinates = coordinates;
        this.enginePower = enginePower;
        this.fuelConsumption = fuelConsumption;
        this.type = type;
        this.fuelType = fuelType;
    }

    private static int generateId() {
        if (ids.isEmpty()) {
            ids.add(1);
            return 1;
        }

        int expectedId = 1;
        List<Integer> sorted = ids.stream().sorted().collect(Collectors.toList());

        for (int id : sorted) {
            if (id != expectedId) {
                ids.add(expectedId);
                return expectedId;
            }
            expectedId++;
        }

        ids.add(expectedId);
        return expectedId;
    }

    public static void clearIds() {
        ids.clear();
    }

    public static Set<Integer> getIds() {
        return ids;
    }

    public void setId(Integer id) {
        if (id != null) {
            this.id = id;
            ids.add(id);
        }
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

    @XmlElement
    public Integer getId() {
        return id;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    @XmlElement
    public Coordinates getCoordinates() {
        return coordinates;
    }

    @XmlElement
    public float getEnginePower() {
        return enginePower;
    }

    @XmlElement
    public Integer getFuelConsumption() {
        return fuelConsumption;
    }

    @XmlElement
    public VehicleType getType() {
        return type;
    }

    @XmlElement
    public FuelType getFuelType() {
        return fuelType;
    }

    @XmlElement
    @XmlJavaTypeAdapter(ZonedDateTimeAdapter.class)
    public ZonedDateTime getCreationDateObject() {
        return creationDate;
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
        return String.format("Vehicle{id=%d, name='%s', coords=(%d,%d), engine=%.1f, fuel=%s, type=%s}",
                id, name,
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
        return copy;
    }
}