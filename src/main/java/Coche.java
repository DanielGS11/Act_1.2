import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.List;

@XmlRootElement
public class Coche {
    @XmlAttribute
    private int id;

    @XmlElement
    private String matricula;

    @XmlElement
    private String marca;

    @XmlElement
    private String modelo;

    @XmlElementWrapper(name = "equipamiento")
    @XmlElement(name = "extra")
    private List<String> equipamiento;

    public Coche() {
    }

    public Coche(int id, String matricula, String marca, String modelo, List<String> equipamiento) {
        this.id = id;
        this.matricula = matricula;
        this.marca = marca;
        this.modelo = modelo;
        this.equipamiento = equipamiento;
    }

    public String getMatricula() {
        return matricula;
    }

    @Override
    public String toString() {
        return String.format("\nID: %d\nMatrícula: %s\nMarca: %s\nModelo: %s\nEquipamiento Incluido: %s",
                id, matricula, marca, modelo, equipamiento.isEmpty() ? "Ninguno" : equipamiento);
    }
}
