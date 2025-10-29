import jakarta.xml.bind.annotation.*;

import java.util.List;

/*
Clase Coche con un id que se autoasigna cuando se añade un coche a la base de datos
(Metodo ubicado en la Clase FuncionesConcesionario)
 */
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

    //Element Wrapper para poder crear una etiqueta equipamiento y que cada dato de dentro tenga la siguiente etiqueta
    @XmlElementWrapper(name = "equipamiento")
    @XmlElement(name = "extra")
    private List<String> equipamiento;

    //Constructor vacío para el marshaller y unmarsaller
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

    /*
    Aqui se modifica el id del coche en caso de borrar uno de la Base de Datos u ordenarla
    NOTA: XMLTransient hace que los marshallers y unmarshallers lo ignoren, ya que los setters producen error en estos
     */
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

    //Metodo toString para la clase coche que tambien servira para generar el informe
    @Override
    public String toString() {
        return String.format("\tID: %d\n\tMatrícula: %s\n\tMarca: %s\n\tModelo: %s\n\tEquipamiento Incluido: %s\n",
                /*
                NOTA: Ya que el equipamiento puede estar vacío, en vez de dejarlo como [],
                hago que en su lugar devuelva "Ninguno" con la comprobación "?". Esto se podria hacer tambien con ifs
                 */
                id, matricula, marca, modelo, equipamiento.isEmpty() ? "Ninguno" : equipamiento);
    }
}
