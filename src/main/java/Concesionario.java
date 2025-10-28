import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.nio.file.Path;
import java.util.*;

@XmlRootElement
public class Concesionario {

    @XmlElement(name = "coche")
    public List<Coche> coches;

    public Concesionario() {
    }
}

