package common;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

@XmlType(propOrder = {"x", "y"})
public class Coordinates implements Serializable {
    private static final long serialVersionUID = 1L;

    private long x;
    private Integer y;

    public Coordinates() {
    }

    public Coordinates(long x, Integer y) {
        this.x = x;
        setY(y);
    }

    public void setX(long x) {
        this.x = x;
    }

    public void setY(Integer y) {
        if (y == null) {
            throw new IllegalArgumentException("Координата y не может быть null");
        }
        this.y = y;
    }

    @XmlElement
    public long getX() {
        return x;
    }

    @XmlElement
    public Integer getY() {
        return y;
    }

    public Coordinates copy() {
        return new Coordinates(this.x, this.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Coordinates that = (Coordinates) obj;
        return x == that.x && y.equals(that.y);
    }

    @Override
    public int hashCode() {
        return (int) x + y.hashCode();
    }
}