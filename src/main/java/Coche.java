import java.util.List;

public class Coche {
    private int id;
    private String matricula;
    private String marca;
    private String modelo;
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

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getEquipamiento() {
        return equipamiento;
    }

    public void setEquipamiento(List<String> equipamiento) {
        this.equipamiento = equipamiento;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    @Override
    public String toString() {
        return String.format("\nID: %d\nMatrícula: %s\nMarca: %s\nModelo: %s\nEquipamiento Incluido: %s",
                id, matricula, marca, modelo, equipamiento.isEmpty() ? "Ninguno" : equipamiento);
    }
}
