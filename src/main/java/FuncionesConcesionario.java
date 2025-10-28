import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class FuncionesConcesionario {
    public Concesionario c = new Concesionario();

    public List<Path> rutasFicheros = new ArrayList<>();

    public List<String> lineas = new ArrayList<>();

    public void cargarCSV() {
        for (int i = 1; i < leerCSV().size(); i++) {
            List<String> datosCoche = new ArrayList<>(List.of(lineas.get(i).trim().split(";")));

            if (datosCoche.size() == 3) {
                datosCoche.add("");
            }

            if (c.coches.stream().noneMatch(c -> c.getMatricula().equals(datosCoche.getFirst()))) {
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

    public void insertarCoche() {
        unmarshalXML();

        Scanner sc = new Scanner(System.in);

        System.out.println("Introduzca la Matricula del nuevo Coche");
        String matricula = sc.nextLine().toUpperCase();

        if (c.coches.stream().anyMatch(c -> c.getMatricula().equals(matricula))) {
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
                System.out.println("¿Cuantos extras lleva su coche?");
                int extras = Integer.parseInt(sc.nextLine());
                List<String> equipamiento = new ArrayList<>();

                System.out.println("Introduzca los equipamientos extra del coche (1 por cada linea)");
                for (int i = 0; i < extras; i++) {
                    String extra = sc.nextLine();
                    equipamiento.add(extra.trim().toUpperCase().charAt(0) + extra.trim().substring(1));
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

        c.coches.sort(Comparator.comparing(Coche::getMatricula));

        for (int i = 0; i < c.coches.size(); i++) {
            c.coches.get(i).setId(i + 1);
        }

        marshalXML();
    }

    public void borrarPorMatricula() {
        unmarshalXML();
        Scanner sc = new Scanner(System.in);

        System.out.println("Introduzca la matricula del coche a borrar");
        String matricula = sc.nextLine().toUpperCase();

        Coche coche = c.coches.stream().filter(c -> c.getMatricula().equals(matricula.trim())).findFirst().orElse(null);
        if (coche == null) {
            System.out.printf("El coche con la matricula '%s' no existe en la base de datos\n", matricula);

        } else {
            c.coches.subList(c.coches.indexOf(coche), c.coches.size()).forEach(c -> {
                c.setId(c.getId() - 1);
            });

            c.coches.removeIf(c -> c.getMatricula().equals(matricula.trim()));

            marshalXML();

        }
    }

    public void modificarRegistro() {
        unmarshalXML();
        Scanner sc = new Scanner(System.in);

        System.out.println("Introduzca la Matricula del Coche a Modificar");
        String matricula = sc.nextLine().toUpperCase();

        if (c.coches.stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
            System.out.printf("La matricula %s no existe en la base de datos\n", matricula);
        } else {
            Coche coche = c.coches.stream().filter(c -> c.getMatricula()
                    .equals(matricula.trim())).findFirst().orElse(null);
            System.out.println("Estos son los Datos del Coche:\n" + coche);

            System.out.println("""
                    Ahora Introduzca los nuevos datos del coche (En caso de querer dejarlo igual, no ponga nada y pulse intro)
                    Introduzca la nueva marca:""");
            String marcaModificado = sc.nextLine();
            if (marcaModificado.isEmpty()) {
                marcaModificado = coche.getMarca();
            } else {
                marcaModificado = marcaModificado.toUpperCase().charAt(0) + marcaModificado.trim().substring(1);
            }

            System.out.println("Introduzca el nuevo modelo:");
            String modeloModificado = sc.nextLine();
            if (modeloModificado.isEmpty()) {
                modeloModificado = coche.getMarca();
            } else {
                modeloModificado = modeloModificado.toUpperCase().charAt(0) + modeloModificado.trim().substring(1);
            }

            System.out.println("¿LLeva equipamiento extra? SI/NO");
            String ans = sc.nextLine();
            List<String> equipamientoModificado = new ArrayList<>();

            if (ans.equalsIgnoreCase("SI")) {
                System.out.println("¿LLeva extras Nuevos? SI/NO");
                ans = sc.nextLine();

                if (ans.equalsIgnoreCase("SI")) {
                    System.out.println("¿Cuantos extras lleva su nuevo coche? (Si los sigue llevando, incluidos los anteriores)");
                    int extrasModificado = Integer.parseInt(sc.nextLine());

                    System.out.println("Introduzca los equipamientos extra del nuevo coche (1 por cada linea)");
                    for (int i = 0; i < extrasModificado; i++) {
                        String extra = sc.nextLine();
                        equipamientoModificado.add(extra.toUpperCase().charAt(0) + extra.trim().substring(1));
                    }

                } else if (ans.equalsIgnoreCase("NO")) {
                    equipamientoModificado = coche.getEquipamiento();
                }
            }

            Coche cocheModificado = new Coche(coche.getId(), coche.getMatricula(), marcaModificado, modeloModificado, equipamientoModificado);

            c.coches.add(c.coches.indexOf(coche), cocheModificado);
            c.coches.remove(coche);

            System.out.println("Coche Modificado con Exito");
            marshalXML();
        }
    }

    // ------------------------ METODOS AUXILIARES ---------------------------------
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
        Coche coche = new Coche(c.coches.size() + 1, matricula, marca, modelo, equipamiento);
        c.coches.add(coche);
    }

    public void marshalXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);

            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File xml = new File(cargarPaths().getFirst().toString());

            marshaller.marshal(FuncionesConcesionario.class, xml);
        } catch (JAXBException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void unmarshalXML() {
        if (c.coches.isEmpty()) {
            try {
                JAXBContext context = JAXBContext.newInstance(Concesionario.class);

                Unmarshaller unmarshaller = context.createUnmarshaller();

                File xml = new File(cargarPaths().getFirst().toString());

                Concesionario concesionario = (Concesionario) unmarshaller.unmarshal(xml);

                c.coches = concesionario.coches;

            } catch (JAXBException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
