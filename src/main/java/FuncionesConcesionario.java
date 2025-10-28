import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class FuncionesConcesionario {
    private Concesionario concesionario = new Concesionario();

    private List<Path> rutasFicheros = new ArrayList<>();

    private List<String> lineas = new ArrayList<>();

    public void cargarCSV() {
        for (int i = 1; i < leerCSV().size(); i++) {
            List<String> datosCoche = new ArrayList<>(List.of(lineas.get(i).trim().split(";")));

            if (datosCoche.size() == 3) {
                datosCoche.add("");
            }

            if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(datosCoche.getFirst()))) {
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
        System.out.printf("Fichero '%s' cargado con exito\n", rutasFicheros.getFirst());
    }

    public void insertarCoche() {
        unmarshalXML();

        Scanner sc = new Scanner(System.in);

        System.out.print("Introduzca la Matricula del nuevo coche: ");
        String matricula = sc.nextLine().toUpperCase();

        if (concesionario.getCoches().stream().anyMatch(c -> c.getMatricula().equals(matricula))) {
            System.out.println("La matricula ya existe");

        } else {
            System.out.print("Introduzca la marca del nuevo coche: ");
            String marca = sc.nextLine();
            marca = marca.toUpperCase().charAt(0) + marca.substring(1);

            System.out.print("Introduzca el modelo del nuevo coche: ");
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

        concesionario.getCoches().sort(Comparator.comparing(Coche::getMatricula));

        for (int i = 0; i < concesionario.getCoches().size(); i++) {
            concesionario.getCoches().get(i).setId(i + 1);
        }

        marshalXML();
    }

    public void borrarPorMatricula() {
        unmarshalXML();
        Scanner sc = new Scanner(System.in);

        System.out.print("Introduzca la matricula del coche a borrar: ");
        String matricula = sc.nextLine().toUpperCase();

        Coche coche = concesionario.getCoches().stream().filter(c -> c.getMatricula().equals(matricula.trim())).findFirst().orElse(null);
        if (coche == null) {
            System.out.printf("El coche con la matricula '%s' no existe en la base de datos\n", matricula);

        } else {
            concesionario.getCoches().subList(concesionario.getCoches().indexOf(coche), concesionario.getCoches().size()).forEach(c -> {
                c.setId(c.getId() - 1);
            });

            concesionario.getCoches().removeIf(c -> c.getMatricula().equals(matricula.trim()));

            marshalXML();

        }
    }

    public void modificarRegistro() {
        unmarshalXML();
        Scanner sc = new Scanner(System.in);

        System.out.print("Introduzca la matricula del coche a modificar: ");
        String matricula = sc.nextLine().toUpperCase();

        if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
            System.out.printf("La matricula %s no existe en la base de datos\n", matricula);
        } else {
            Coche coche = concesionario.getCoches().stream().filter(c -> c.getMatricula()
                    .equals(matricula.trim())).findFirst().orElse(null);
            System.out.println("Estos son los Datos del Coche:\n" + coche);

            System.out.print("""
                    Ahora Introduzca los nuevos datos del coche (En caso de querer dejarlo igual, no ponga nada y pulse intro)
                    Introduzca la nueva marca: """);
            String marcaModificado = sc.nextLine();
            if (marcaModificado.isEmpty()) {
                marcaModificado = coche.getMarca();
            } else {
                marcaModificado = marcaModificado.toUpperCase().charAt(0) + marcaModificado.trim().substring(1);
            }

            System.out.print("Introduzca el nuevo modelo: ");
            String modeloModificado = sc.nextLine();
            if (modeloModificado.isEmpty()) {
                modeloModificado = coche.getModelo();
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

            concesionario.getCoches().add(concesionario.getCoches().indexOf(coche), cocheModificado);
            concesionario.getCoches().remove(coche);

            System.out.println("Coche Modificado con Exito");
            marshalXML();
        }
    }

    public void exportarJSON(int opc) {
        switch (opc) {
            case 1:
                unmarshalXML();
                marshalJSON();
                System.out.printf("Base de datos '%s' exportada a '%s' con exito\n",
                        cargarPaths().getFirst(), rutasFicheros.get(2));
                break;

            case 2:
                Scanner sc = new Scanner(System.in);
                unmarshalXML();

                System.out.print("Introduzca la matricula del coche a exportar: ");
                String matricula = sc.nextLine().toUpperCase();

                if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
                    System.out.printf("La matricula '%s' no existe en la base de datos\n", matricula);

                } else {
                    concesionario.getCoches().removeIf(c -> !c.getMatricula().equals(matricula.trim()));
                    concesionario.getCoches().getFirst().setId(1);
                    marshalJSON();
                    System.out.printf("Base de datos '%s' solo con el coche de matricula '%s'\n exportada a '%s' con exito\n",
                            cargarPaths().getFirst(), matricula, rutasFicheros.get(2));
                }
                break;
        }

    }

    public void importarJSON(int opc) {
        switch (opc) {
            case 1:
                unmarshalJSON();
                marshalXML();
                System.out.printf("Base de datos '%s' importada a '%s' con exito\n",
                        rutasFicheros.get(2), cargarPaths().getFirst());
                break;

            case 2:
                Scanner sc = new Scanner(System.in);
                unmarshalJSON();

                System.out.print("Introduzca la matricula del coche a importar: ");
                String matricula = sc.nextLine().toUpperCase();

                if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
                    System.out.printf("La matricula '%s' no existe en la base de datos\n", matricula);

                } else {
                    concesionario.getCoches().removeIf(c -> !c.getMatricula().equals(matricula.trim()));
                    concesionario.getCoches().getFirst().setId(1);
                    marshalXML();
                    System.out.printf("Base de datos '%s' solo con el coche de matricula '%s'\n exportada a '%s' con exito\n",
                            cargarPaths().getFirst(), matricula, rutasFicheros.get(2));
                }
                break;
        }

    }

    public void generarInforme() {
        unmarshalXML();
        int totalCoches = concesionario.getCoches().size();

        Map<String, List<Coche>> cochesPorMarca = new HashMap<>();

        for (Coche c : concesionario.getCoches()) {
            if (!cochesPorMarca.containsKey(c.getMarca())) {
                cochesPorMarca.put(c.getMarca(), new ArrayList<>(List.of(c)));
            } else {
                cochesPorMarca.get(c.getMarca()).add(c);
            }
        }
        String cochesAgrupados = cochesPorMarca.entrySet().stream()
                .map(e -> String.format("-- %s :\n\t%s\n", e.getKey(),
                        e.getValue().stream()
                                .map(v -> String.format("%s", v)).collect(Collectors.joining())
                ))
                .collect(Collectors.joining());

        List<String> extrasCoches = new ArrayList<>();
        concesionario.getCoches().forEach(c -> extrasCoches.addAll(c.getEquipamiento()));

        int repeticionEquipamiento = 0;
        String equipamientoMasRepetido = "";

        for (int i = 0; i < extrasCoches.size(); i++) {
            String palabraTemp = extrasCoches.get(i);
            AtomicInteger repeticionPalabraTemp = new AtomicInteger();

            extrasCoches.forEach(s -> {
                if (s.equals(palabraTemp)) {
                    repeticionPalabraTemp.getAndIncrement();
                }
            });

            extrasCoches.removeIf(s -> s.equals(palabraTemp));

            if (repeticionPalabraTemp.get() > repeticionEquipamiento) {
                equipamientoMasRepetido = palabraTemp;
                repeticionEquipamiento = repeticionPalabraTemp.get();
            }
        }

        try {
            Files.write(cargarPaths().getLast(), List.of("- Numero Total de Coches: " + totalCoches,
                            "- Coches Agrupados Por Marca:\n " + cochesAgrupados
                            , "- Equipamiento que mas se repite: " + equipamientoMasRepetido
                                    + " (" + repeticionEquipamiento + " veces)\n"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        System.out.printf("informe '%s' generado con exito\n", rutasFicheros.getLast());
    }

    // ------------------------ METODOS AUXILIARES ---------------------------------
    private List<Path> cargarPaths() {
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

    private List<String> leerCSV() {
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

    private void addCoche(String matricula, String marca, String modelo, List<String> equipamiento) {
        Coche coche = new Coche(concesionario.getCoches().size() + 1, matricula, marca, modelo, equipamiento);
        concesionario.getCoches().add(coche);
    }

    private void marshalXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);

            Marshaller marshaller = context.createMarshaller();

            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            File xml = new File(cargarPaths().getFirst().toString());

            marshaller.marshal(concesionario, xml);
        } catch (JAXBException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void marshalJSON() {
        ObjectMapper om = new ObjectMapper();

        //Dar formato a salida
        om.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            //Serializar a JSON
            om.writeValue(cargarPaths().get(2).toFile(), concesionario);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }


    }

    private void unmarshalXML() {
        if (concesionario.getCoches().isEmpty()) {
            try {
                JAXBContext context = JAXBContext.newInstance(Concesionario.class);

                Unmarshaller unmarshaller = context.createUnmarshaller();

                File xml = new File(cargarPaths().getFirst().toString());

                concesionario = (Concesionario) unmarshaller.unmarshal(xml);

            } catch (JAXBException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    public void unmarshalJSON() {
        ObjectMapper om = new ObjectMapper();

        //Dar formato a salida
        om.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            //Serializar a JSON
            concesionario = om.readValue(cargarPaths().get(2).toFile(), Concesionario.class);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }


    }
}
