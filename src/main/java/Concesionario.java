import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
                    Paths.get(prop.getProperty("path_JSON")),
                    Paths.get(prop.getProperty("path_Informe"))));


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

    public void addCoche(String matricula, String marca, String modelo, List<String> equipamiento) {
        Coche coche = new Coche(coches.size() + 1, matricula, marca, modelo, equipamiento);
        coches.add(coche);
    }

    public void cargarCSV() {
        for (int i = 1; i < leerCSV().size(); i++) {
            List<String> datosCoche = new ArrayList<>(List.of(lineas.get(i).trim().split(";")));

            if (datosCoche.size() == 3) {
                datosCoche.add("");
            }

            if (coches.stream().noneMatch(c -> c.getMatricula().equals(datosCoche.getFirst()))) {
                addCoche(datosCoche.getFirst(),
                        datosCoche.get(1),
                        datosCoche.get(2),
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

    public void marshalXML() {
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

    public void unmarshalXML() {
        if (coches.isEmpty()) {
            try {
                JAXBContext context = JAXBContext.newInstance(Concesionario.class);

                Unmarshaller unmarshaller = context.createUnmarshaller();

                File xml = new File(cargarPaths().getFirst().toString());

                Concesionario c = (Concesionario) unmarshaller.unmarshal(xml);

                coches = c.coches;

            } catch (JAXBException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void insertarCoche() {
        unmarshalXML();

        Scanner sc = new Scanner(System.in);

        System.out.println("Introduzca la Matricula del nuevo Coche");
        String matricula = sc.nextLine().trim().toUpperCase();

        if (coches.stream().anyMatch(c -> c.getMatricula().equals(matricula))) {
            System.out.println("La matricula ya existe");

        } else {
            System.out.println("Introduzca la marca del nuevo Coche");
            String marca = sc.nextLine();
            marca = marca.toUpperCase().charAt(0) + marca.substring(1);

            System.out.println("Introduzca el modelo del nuevo Coche");
            String modelo = sc.nextLine();
            modelo = modelo.toUpperCase().charAt(0) + modelo.substring(1);

            System.out.println("¿Dispone el coche de Equipamiento extra? (GPS, Pantalla...) SI/NO");
            String ans = sc.nextLine();

            if (ans.equalsIgnoreCase("SI")) {
                System.out.println("Cuantos extras lleva su coche?");
                int extras = Integer.parseInt(sc.nextLine());
                List<String> equipamiento = new ArrayList<>();

                System.out.println("Introduzca los equipamientos extra del coche (1 por cada linea)");
                for (int i = 0; i < extras; i++) {
                    String extra = sc.nextLine().trim();
                    equipamiento.add(extra.toUpperCase().charAt(0) + extra.substring(1));
                }

                addCoche(matricula, marca, modelo, equipamiento);
            } else {
                addCoche(matricula, marca, modelo, new ArrayList<>());
            }

            marshalXML();

            System.out.println("Coche añadido correctamente a la base de datos");
        }
    }

    public void ordenarCoches() {
        unmarshalXML();

        coches.sort(Comparator.comparing(Coche::getMatricula));

        marshalXML();
    }

    public void borrarPorMatricula() {
        unmarshalXML();
        Scanner sc = new Scanner(System.in);

        System.out.println("Introduzca la matricula del coche a borrar");
        String matricula = sc.nextLine().trim().toUpperCase();

        Coche coche = coches.stream().filter(c -> c.getMatricula().equals(matricula)).findFirst().orElse(null);
        if (coche == null) {
            System.out.printf("El coche con la matricula '%s' no existe en la base de datos\n", matricula);

        } else {
            coches.subList(coches.indexOf(coche), coches.size()).forEach(c -> {
                c.setId(c.getId() - 1);
            });

        coches.removeIf(c -> c.getMatricula().equals(matricula));

        marshalXML();

    }
}

}

