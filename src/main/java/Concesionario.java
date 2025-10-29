import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;


import java.util.*;

//Clase concesionario que contiene los coches en una lista con la etiqueta XML "Coche" para cada elemento de esta
@XmlRootElement
public class Concesionario {

    @XmlElement(name = "coche")
    public List<Coche> coches = new ArrayList<>();

    public Concesionario() {
    }

    public List<Coche> getCoches() {
        return coches;
    }
}

