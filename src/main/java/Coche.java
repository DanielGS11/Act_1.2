import jakarta.xml.bind.annotation.*;

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

    public int getId() {
        return id;
    }

    @XmlTransient
    public void setId(int id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public List<String> getEquipamiento() {
        return equipamiento;
    }

    @Override
    public String toString() {
        return String.format("ID: %d\n\tMatrícula: %s\n\tMarca: %s\n\tModelo: %s\n\tEquipamiento Incluido: %s\n",
                id, matricula, marca, modelo, equipamiento.isEmpty() ? "Ninguno" : equipamiento);
    }
}
