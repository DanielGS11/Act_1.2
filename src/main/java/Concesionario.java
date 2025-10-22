import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@XmlRootElement
public class Concesionario {

    @XmlElement(name = "coche")
    private List<Coche> coches = new ArrayList<>();

    private List<Path> rutasFicheros = new ArrayList<>();

    private List<String> lineas = new ArrayList<>();

    public Concesionario() {
    }

    public List<Path> cargarPaths() {
        if (!rutasFicheros.isEmpty()) {
            return rutasFicheros;
        }

        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream("config.properties"));

            rutasFicheros.addAll(List.of(Paths.get(prop.getProperty("path_XML")),
                    Paths.get(prop.getProperty("path_CSV")),
                    Paths.get(prop.getProperty("path_JSON"))));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return rutasFicheros;
    }

    public List<String> leerCSV() {
        if (!lineas.isEmpty()) {
            return lineas;
        }

        try {
            lineas = Files.readAllLines(cargarPaths().get(1));
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        return lineas;
    }

    public void addCoche(int id, String matricula, String marca, String modelo, List<String> equipamiento) {
        Coche coche = new Coche(id, matricula, marca, modelo, equipamiento);
        coches.add(coche);
    }

    public void importarCSV() {
        for (int i = 1; i < leerCSV().size(); i++) {
            List<String> datosCoche = new ArrayList<>(List.of(lineas.get(i).split(";")));

            if (datosCoche.size() == 3) {
                datosCoche.add("");
            }

            if (coches.stream().noneMatch(c -> c.getMatricula().equals(datosCoche.getFirst()))) {
                addCoche(coches.size() + 1, datosCoche.getFirst(), datosCoche.get(1), datosCoche.get(2),
                        datosCoche.get(3).isEmpty() ? new ArrayList<>() :
                                new ArrayList<>(List.of(datosCoche.getLast().trim().split("[|]"))));
            } else {
                System.out.printf("""
                        ----------------------------------------------
                        Matricula Duplicada encontrada en linea %d: %s
                        ----------------------------------------------
                        """, i + 1, datosCoche.getFirst());
            }
        }

        marshalXML();
    }

    private void marshalXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            File xml = new File(cargarPaths().getFirst().toString());

            marshaller.marshal(this, xml);
        } catch (JAXBException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

}

