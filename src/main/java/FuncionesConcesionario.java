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

/*
Esta clase se encargara de los metodos que necesita nuestra base de datos, creara un objeto concesionario para
acceder a su lista de coches y creara una lista con las rutas de los ficheros de config.properties y otra que cargue
las lineas del fichero CSV

NOTA: los metodos auxiliares estan agrupados en el final del archivo
 */
public class FuncionesConcesionario {
    private Concesionario concesionario = new Concesionario();

    private List<Path> rutasFicheros = new ArrayList<>();

    private List<String> lineas = new ArrayList<>();

    //Metodo para cargar el archivo CSV en la Base de Datos
    public void cargarCSV() {
    /*
    Este metodo llama al metodo auxiliar que se encarga de leer el csv para recoger las lineas que nos da y
    obtener los datos de los coches 1 a 1
     */
        for (int i = 1; i < leerCSV().size(); i++) {
            List<String> datosCoche = new ArrayList<>(List.of(lineas.get(i).trim().split(";")));

            /*
            NOTA: si un coche no tiene equipamiento, tendria menos espacio en la lista de datos, impidiendo mas tarde
            configurar la adicion del coche de forma eficiente, por lo que en esops casos se le añade un espacio en
            blanco a la lista de datos de este, haciendo que se pueda crear el registro de un coche sin extras
             */
            if (datosCoche.size() == 3) {
                datosCoche.add("");
            }

            //Comprobacion de que no se repitan matriculas de coches, quedandose solo con la primera registrada
            if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(datosCoche.getFirst()))) {
                //LLama a un metodo auxiliar para añadir coches y le da los datos (La ID se autoasigna en el metodo)
                addCoche(datosCoche.getFirst(),
                        datosCoche.get(1),
                        datosCoche.get(2),
                        /*
                        Al igual que en el toString() de la Clase coche, si el espacio correspondiente al equipamiento
                        esta vacio, devuelve una lista vacia creando asi una etiqueta sin contenido y ayudando
                        a ver en el informe si el coche tiene o no equipamiento
                         */
                        datosCoche.get(3).isEmpty() ? new ArrayList<>() :
                                new ArrayList<>(List.of(datosCoche.getLast().trim().split("[|]"))));
            } else {
                /*
                Aqui avisa que matricula se repite y en que linea del CSV de ubica
                NOTA: la linea es i + 1 porque el programa lee las lineas empezando por el 0, por lo que lo que
                nosotros veriamos como linea 3 por ejemplo, el programa lo ve como linea 2
                 */

                System.out.printf("""
                        ----------------------------------------------
                        Matricula Duplicada encontrada en linea %d: %s
                        ----------------------------------------------
                        """, i + 1, datosCoche.getFirst());
            }
        }
        //Por ultimo, se llama al metodo auxiliar que imprime los datos en la Base de Datos y se Notifica al usuario
        marshalXML();
        System.out.printf("Fichero '%s' cargado con exito\n", rutasFicheros.getFirst());
    }

    //Metodo para insertar un registro de un coche en la Base de Datos
    public void insertarCoche() {
        /*
          Si no se uso el metodo para cargar el CSV, ya que la Base de Datos estaba creada, se llama al
          metodo auxiliar unmarshaller que lee sus datos, esto sirve por si la base de datos tiene registros que no
          estan en el CSV
         */
        unmarshalXML();

        //Creo un scanner para que el usuario instroduzca datos por pantalla
        Scanner sc = new Scanner(System.in);

        System.out.print("Introduzca la Matricula del nuevo coche: ");
        String matricula = sc.nextLine().toUpperCase();

        //Cuando el usuario introdduzca la matricula, que es el campo clave, tenemos que comprobar que no exista
        if (concesionario.getCoches().stream().anyMatch(c -> c.getMatricula().equals(matricula))) {
            System.out.println("La matricula ya existe");

        } else {
            System.out.print("Introduzca la marca del nuevo coche: ");
            String marca = sc.nextLine();
            marca = marca.toUpperCase().charAt(0) + marca.substring(1);

            System.out.print("Introduzca el modelo del nuevo coche: ");
            String modelo = sc.nextLine();
            modelo = modelo.toUpperCase().charAt(0) + modelo.substring(1);

            /*
            Aqui el usuario dice si el coche lleva o no equipamiento, si no lleva, creara la lista vacia, pero en caso
            de que disponga de extras, el usuario debera decir cuantos y cuales son
             */
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

                //Una vez introducidos los datos, se añade el coche con el metodo auxiliar y se notifica al usuario
                addCoche(matricula, marca, modelo, equipamiento);
            } else {
                addCoche(matricula, marca, modelo, new ArrayList<>());
            }

            marshalXML();

            System.out.println("Coche añadido correctamente a la base de datos");
        }
    }

    //Metodo que ordena los coches de la Base de Datos por matricula (Su Campo Clave)
    public void ordenarCoches() {
        //Se cargan los datos de la Base de Datos
        unmarshalXML();

        //Se ordena la lista donde se cargan los datos por matricula
        concesionario.getCoches().sort(Comparator.comparing(Coche::getMatricula));

        //Se les reasigna la id a los coches ya que el que tiene id 3, por ejemplo, puede por orden ir primero
        for (int i = 0; i < concesionario.getCoches().size(); i++) {
            concesionario.getCoches().get(i).setId(i + 1);
        }

        //Se cargan los datos a la Base de Datos
        marshalXML();
    }

    //Metodo para Borrar los Coches por la Base de Datos por matricula
    public void borrarPorMatricula() {
        //Cargamos los datos de la Base de Datos
        unmarshalXML();

        //Creamos un medio para la introduccion de datos por pantalla para el usuario
        Scanner sc = new Scanner(System.in);

        System.out.print("Introduzca la matricula del coche a borrar: ");
        String matricula = sc.nextLine().toUpperCase();

        /*
        Comprobamos que la matricula a borrar exista en la Base de Datos creando un objeto coche para que, en caso
        de que esté, se modifiquen los id de los registros que estaban despues de este sin recorrer toda la lista
         */
        Coche coche = concesionario.getCoches().stream().filter(c -> c.getMatricula().equals(matricula.trim())).findFirst().orElse(null);
        if (coche == null) {
            System.out.printf("El coche con la matricula '%s' no existe en la base de datos\n", matricula);

        } else {
            //al solo eliminar un coche a la vez, restamos 1 a los registros que estaban deespues de este
            concesionario.getCoches().subList(concesionario.getCoches().indexOf(coche), concesionario.getCoches().size()).forEach(c -> {
                c.setId(c.getId() - 1);
            });

            /*
            Hecha esta modificacion a los id de los coches siguientes, borramos el coche en cuestion, cargamos
            la Base de Datos y notificamos al usuario
             */
            concesionario.getCoches().removeIf(c -> c.getMatricula().equals(matricula.trim()));

            marshalXML();
            System.out.printf("Coche con la matricula '%s' borrado con exito\n", matricula);
        }
    }

    //Modificar un coche de la Base de Datos buscandolo por su matricula (Sin modificar la matricula)
    public void modificarRegistro() {
        //Cargamos la Base de Datos y creamos medio de introduccion de datos por pantalla
        unmarshalXML();
        Scanner sc = new Scanner(System.in);

        System.out.print("Introduzca la matricula del coche a modificar: ");
        String matricula = sc.nextLine().toUpperCase();

        //Comprobamos que la matricula del coche a modificar exista
        if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
            System.out.printf("La matricula %s no existe en la base de datos\n", matricula);
        } else {
            /*
            En caso de existir, creamos un objeto coche con los datos del registro, que tambien nos servira
            para borrar el antiguo, y se los mostramos al usuario
             */
            Coche coche = concesionario.getCoches().stream().filter(c -> c.getMatricula()
                    .equals(matricula.trim())).findFirst().orElse(null);
            System.out.println("Estos son los Datos del Coche:\n" + coche);

            /*
            Pedimos los nuevos datos al usuario, pero en caso de no querer cambiar un parametro, simplemente
            pone el que estaba automaticamente
             */
            System.out.print("""
                    Ahora Introduzca los nuevos datos del coche (En caso de querer dejarlo igual, no ponga nada y pulse intro)
                    Introduzca la nueva marca:\s""");
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

            /*
            Preguntamos si tiene o no equipamientos el nuevo registro y si los tiene, preguntamos si tiene
            nuevos equipamientos o solo los que tenia antes, en caso de tener nuevos, el usuario debera ingresar la
            cajntidad, contando tambien los que tenia si los tiene y debera decir cuales son
             */
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

            /*
            hecho esto, creamos un objeto coche con los datos del coche modificado y su id y matricula, buscamos
            la posicion donde estaba este coche y añadimos el nuevo y borramos el viejo, por ultimo, se cargan los
            datos en la base de datos y se notifica al usuario
             */
            Coche cocheModificado = new Coche(coche.getId(), coche.getMatricula(), marcaModificado, modeloModificado, equipamientoModificado);

            concesionario.getCoches().add(concesionario.getCoches().indexOf(coche), cocheModificado);
            concesionario.getCoches().remove(coche);

            marshalXML();
            System.out.println("Coche Modificado con Exito");
        }
    }

    /**
     Metodo para cargar la Base de Datos en un archivo JSON
     @param opc: Opcion de exportacion que ingresa el usuario en el menu de la clase Main
     */
    public void exportarJSON(String opc) {
        /*
        Comprobamos si el usuario quiere exportar toda la Base de Datos, solo 1 registro
        o, si se equivoca, notificarlo
         */
        switch (opc) {
            case "1":
                /*
                Si quiere exportar toda la Base de Datos la cargamos y directamente llamamos al metodo
                que carga y crea e archivo JSON, y lo notificamos al usuario
                 */
                unmarshalXML();
                marshalJSON();
                System.out.printf("Base de datos '%s' exportada a '%s' con exito\n",
                        cargarPaths().getFirst(), rutasFicheros.get(2));
                break;

            case "2":
                /*
                Si solo quiere exportar un registro, cargamos los datos de la Base de Datos, pedimos la matricula
                del registro que quiere exportar, buscamos que exista y borramos los demas registros
                 */
                Scanner sc = new Scanner(System.in);
                unmarshalXML();

                System.out.print("Introduzca la matricula del coche a exportar: ");
                String matricula = sc.nextLine().toUpperCase();

                if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
                    System.out.printf("La matricula '%s' no existe en la base de datos\n", matricula);

                } else {
                    concesionario.getCoches().removeIf(c -> !c.getMatricula().equals(matricula.trim()));
                    concesionario.getCoches().getFirst().setId(1);

                    //Por ultimo creamos y cargamos o actualizamos el archivo JSON y lo notificamos
                    marshalJSON();
                    System.out.printf("Base de datos '%s' solo con el coche de matricula '%s'\n exportada a '%s' con exito\n",
                            cargarPaths().getFirst(), matricula, rutasFicheros.get(2));
                }
                break;

            default:
                System.out.println("Opcion no valida, debe ser 1 o 2");
                break;
        }

    }

    /**
     Metodo para cargar la base de dtaos en un archivo JSON
     @param opc: Opcion de exportacion que ingresa el usuario en el menu de la clase Main
     */
    public void importarJSON(String opc) {
        /*
        Comprobamos si el usuario quiere importar toda la Base de Datos, solo 1 registro
        o, si se equivoca, notificarlo
         */
        switch (opc) {
            case "1":
                /*
                Si quiere importar todo a la Base de Datos, cargamos los datos del archivo JSON mediante un metodo
                auxiliar y cargamos los datos que nos dan en la base de datos Base de Datos
                 */
                unmarshalJSON();
                marshalXML();
                System.out.printf("Base de datos '%s' importada a '%s' con exito\n",
                        rutasFicheros.get(2), cargarPaths().getFirst());
                break;

            case "2":
                /*
                En caso de querer importar solo 1 registro, cargamos el archivo JSON, pedimos al usuario que
                instroduzca la matricula del registro a importar, comprobamos que exista y borramos los demas
                registros
                 */
                Scanner sc = new Scanner(System.in);
                unmarshalJSON();

                System.out.print("Introduzca la matricula del coche a importar: ");
                String matricula = sc.nextLine().toUpperCase();

                if (concesionario.getCoches().stream().noneMatch(c -> c.getMatricula().equals(matricula.trim()))) {
                    System.out.printf("La matricula '%s' no existe en la base de datos\n", matricula);

                } else {
                    concesionario.getCoches().removeIf(c -> !c.getMatricula().equals(matricula.trim()));
                    concesionario.getCoches().getFirst().setId(1);

                    //Por ultimo cargamos los datos en la Base de Datos y notificamos al usuario
                    marshalXML();
                    System.out.printf("Base de datos '%s' solo con el coche de matricula '%s'\n exportada a '%s' con exito\n",
                            cargarPaths().getFirst(), matricula, rutasFicheros.get(2));
                }
                break;

            default:
                System.out.println("Opcion no valida, debe ser 1 o 2");
                break;
        }

    }

    //Metodo para generar un informe de resumen de la Base de Datos
    public void generarInforme() {
        /*
        El informe contendra el numero de coches de la Base de Datos, los coches agrupados por tipo y el/los
        equipamiento/s que mas se repite/n en la Base de Datos.
        Primero se carga la Base de Datos y crearemos 5 variables:
        - totalCoches: almacena la cuenta de cuantos coches hay en la Base de Datos
        - cochesPorMarca: un mapa que guarda las marcas y los coches de cada marca
        - cochesAgrupados: un String que almacena los datos de cochesPorMarca para plasmar en el informe ya ordenado
        - repeticionEquipamiento: un Integer que almacena las veces que se repite el equipamiento mas repetido y/o
            compara si ningun equipamiento se repite o no hay extras en la Base de Datos
        - equipamientoMasRepetido: un StringBuilder que almacena el/los equipamientos mas repetido/s y las veces
        que se repite/n (variable repeticionEquipamiento)
         */
        unmarshalXML();
        /*
        Para el total de coches simplemente necesitamos ver el tamaño
        de la lista de coches cargada de la Base de Datos
         */
        int totalCoches = concesionario.getCoches().size();

        /*
        Creamos el mapa y extraemos como clave la marca del coche, comprobando que solo haya una de cada cada vez,
        y le damos como valor el registro del coche
         */
        Map<String, List<Coche>> cochesPorMarca = new HashMap<>();

        for (Coche c : concesionario.getCoches()) {
            if (!cochesPorMarca.containsKey(c.getMarca())) {
                cochesPorMarca.put(c.getMarca(), new ArrayList<>(List.of(c)));
            } else {
                cochesPorMarca.get(c.getMarca()).add(c);
            }
        }

        /*
        Ahora simplemente le damos un formato al mapa dodne figure
        una lista de las marcas y sus coches en una variable
         */
        String cochesAgrupados = cochesPorMarca.entrySet().stream()
                .map(e -> String.format("-- %s :\n%s\n", e.getKey(),
                        e.getValue().stream()
                                .map(v -> String.format("%s", v)).collect(Collectors.joining())
                ))
                .collect(Collectors.joining());

        //Creamos una variable que almacene cada extra de cada coche, incluso repetidos
        List<String> extrasCoches = new ArrayList<>();
        concesionario.getCoches().forEach(c -> extrasCoches.addAll(c.getEquipamiento()));

        int repeticionEquipamiento = 0;
        StringBuilder equipamientoMasRepetido = new StringBuilder();

        /*
        recorremos la lista de extras y almacenamos temporalmente la palabra que toque y hacemos un contador que
        se incrementara cada vez que aparezca la palabra
         */
        for (int i = 0; i < extrasCoches.size(); i++) {
            StringBuilder palabraTemp = new StringBuilder(extrasCoches.get(i));
            AtomicInteger repeticionPalabraTemp = new AtomicInteger();

            extrasCoches.forEach(s -> {
                if (palabraTemp.toString().equals(s)) {
                    repeticionPalabraTemp.getAndIncrement();
                }
            });

            /*
            Cada vez que termine de contar las repeticiones de una palabra, elimina todas las coincidencias de la lista,
            asi no se recorre mas veces de las necesarias
             */
            extrasCoches.removeIf(s -> palabraTemp.toString().equals(s));

            /*
            Aqui comprobamos que solo se quede el extra que mas se repite y que, en caso de haber mas de 1, los ponga a
            los 2
             */
            if (repeticionPalabraTemp.get() > repeticionEquipamiento) {
                equipamientoMasRepetido = new StringBuilder("\n\t" + palabraTemp);
                repeticionEquipamiento = repeticionPalabraTemp.get();

            } else if (repeticionEquipamiento == repeticionPalabraTemp.get()) {
                equipamientoMasRepetido.append("\n\t" + palabraTemp);
            }
        }

        /*
        Por ultimo, en caso de que no haya equipamientos o no se repita ninguno, el valor se plasmara en el informe
         */
        if (repeticionEquipamiento == 0) {
            equipamientoMasRepetido = new StringBuilder("Ningun Coche Dispone de Equipamiento");

        } else if (repeticionEquipamiento == 1) {
            equipamientoMasRepetido = new StringBuilder("Todos se repiten al menos 1 vez");
        } else {
            //Añadimos el contador al final para generalizar las veces que se repite un extra si hay varios
            equipamientoMasRepetido.append(String.format("\n\t--- Se repite(n) %d veces en la Base de Datos ---", repeticionEquipamiento));
        }

        try {
            //configurados los valores, creamos y plasmamos los valores en el informe y lo notificamos al usuario
            Files.write(cargarPaths().getLast(), List.of("- Numero Total de Coches: " + totalCoches,
                            "- Coches Agrupados Por Marca:\n " + cochesAgrupados
                            , "- Equipamiento que mas se repite: " + equipamientoMasRepetido + "\n"),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            System.out.printf("informe '%s' generado con exito\n", rutasFicheros.getLast());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ------------------------ METODOS AUXILIARES ---------------------------------
    //Metodo Auxiliar que carga las rutas del fichero config.properties en la lista de rutas
    private List<Path> cargarPaths() {
        /*
        en caso de que este metodo se hubiese llamado antes y ya se hubiese cargado,
        no hay necesidad de hacelo de nuevo
         */
        if (!rutasFicheros.isEmpty()) {
            return rutasFicheros;
        }

        //Esta variable se encargara de recoger las propiedades del fichero2
        Properties prop = new Properties();

        try {
            //cargamos las propiedades del fichero y las asignamos a la lista
            prop.load(new FileInputStream("config.properties"));

            rutasFicheros.addAll(List.of(Paths.get(prop.getProperty("path_XML")),
                    Paths.get(prop.getProperty("path_CSV")),
                    Paths.get(prop.getProperty("path_JSON")),
                    Paths.get(prop.getProperty("path_Informe"))));


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        //Devolvemos la lista ya cargada
        return rutasFicheros;
    }

    //Metodo Auxilar para cargar y leer las lineas del fichero CSV
    private List<String> leerCSV() {
        //EN caso de haberse hecho antes, no hay necesidad de leer de nuevo el CSV
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
        try {
            Files.deleteIfExists(cargarPaths().get(2));

            System.out.println(cargarPaths().get(2).toFile().exists());
            ObjectMapper om = new ObjectMapper();

            //Dar formato a salida
            om.enable(SerializationFeature.INDENT_OUTPUT);

            //Serializar a JSON
            om.writeValue(rutasFicheros.get(2).toFile(), concesionario);

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }


    }

    private void unmarshalXML() {
        try {
            JAXBContext context = JAXBContext.newInstance(Concesionario.class);

            Unmarshaller unmarshaller = context.createUnmarshaller();

            File xml = new File(cargarPaths().getFirst().toString());

            concesionario = (Concesionario) unmarshaller.unmarshal(xml);

        } catch (JAXBException e) {
            System.out.println("Error: " + e.getMessage());
        }

    }

    private void unmarshalJSON() {
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
