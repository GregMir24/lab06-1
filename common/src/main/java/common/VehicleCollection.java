package common;

import common.Vehicle;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlRootElement
public class VehicleCollection {

    private List<VehicleEntry> entries = new ArrayList<>();

    public VehicleCollection() {}

    public VehicleCollection(HashMap<String, Vehicle> vehicles) {
        for (Map.Entry<String, Vehicle> entry : vehicles.entrySet()) {
            entries.add(new VehicleEntry(entry.getKey(), entry.getValue()));
        }
    }

    @XmlElement(name = "entry")
    public List<VehicleEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<VehicleEntry> entries) {
        this.entries = entries;
    }

    public HashMap<String, Vehicle> toHashMap() {
        HashMap<String, Vehicle> map = new HashMap<>();
        for (VehicleEntry entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }

    public static class VehicleEntry {
        private String key;
        private Vehicle value;

        public VehicleEntry() {}

        public VehicleEntry(String key, Vehicle value) {
            this.key = key;
            this.value = value;
        }

        @XmlElement
        public String getKey() { return key; }
        public void setKey(String key) { this.key = key; }

        @XmlElement
        public Vehicle getValue() { return value; }
        public void setValue(Vehicle value) { this.value = value; }
    }
}