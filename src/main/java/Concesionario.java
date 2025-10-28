import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


import java.util.*;

@XmlRootElement
public class Concesionario {

    @XmlElement(name = "coche")
    @JsonbProperty("coche")
    public List<Coche> coches = new ArrayList<>();

    public Concesionario() {}

    public List<Coche> getCoches() {
        return coches;
    }
}

