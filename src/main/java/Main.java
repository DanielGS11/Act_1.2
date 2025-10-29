import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        //Creamos un objeto para llamar a los metodos y llamamos al menu asignandol el objeto
        FuncionesConcesionario f = new FuncionesConcesionario();

        menu(f);
    }

    /**
     * @param f: Objeto de las funciones del Concesionario
     */
    private static void menu(FuncionesConcesionario f) {
        //Creamos una entrada de datos por pantalla, la variable que lo recogera y el menu que el usuario vera
        Scanner sc = new Scanner(System.in);

        System.out.print("""
                *** CONCESIONARIO DE DANIEL GARCIA ***
                - 1) Importar Coches a la Base de Datos desde un Archivo CSV
                - 2) Insertar un Nuevo Coche a la Base de Datos
                - 3) Ordenar Los coches de la Base de Datos por Matricula
                - 4) Borrar un Coche de la Base de Datos Buscando por Matricula
                - 5) Modificar un Coche de la Base de Datos Buscando por Matricula
                - 6) Exportar la Base de Datos a un Archivo JSON
                - 7) Importar la Base de Datos a un Archivo JSON
                - 8) Generar un Informe de Resumen de la Base de Datos
                - 9) Salir
                
                Introduzca la Opcion a Realizar (SOLO NUMEROS DEL 1 AL 9):\s""");

        String ans = sc.nextLine();

        //LLamamos al metodo que ejecutara los metodos que nos ofrece el objeto que creamos en el main, asignandolo
        opcionesMenu(f, sc, ans);
    }

    /**
     * @param f:   Objeto de las funciones del Concesionario
     * @param sc:  Entrada de datos por pantalla
     * @param ans: Variable que recoge los datos introducidos por pantalla y elige las opciones
     */
    private static void opcionesMenu(FuncionesConcesionario f, Scanner sc, String ans) {
        /*
        Creamos un switch que, dependiendo de la opcion, la ejecute o, si el usuario se equivoco, se lo haga saber,
        ademas de una opcion para salir del programa.
        TRas cada ejecucion de un metodo o equivocacion del usuario, excepto si decide salir, volvera al metodo menu
        para elegir mas opciones
         */
        switch (ans) {
            case "1":
                f.cargarCSV();
                break;

            case "2":
                f.insertarCoche();
                break;

            case "3":
                f.ordenarCoches();
                break;

            case "4":
                f.borrarPorMatricula();
                break;

            case "5":
                f.modificarRegistro();
                break;

            /*
            En los casos 6 y 7 (Exportar e Importar el fichero JSON) se pide de nuevo introducir datos ya que el
            usuario decidira si importar/exportar toda la base de datos o solo un registro
             */
            case "6":
                System.out.print("""
                        - 1) Exportar toda la Base de Datos
                        - 2) Exportar un solo Coche de la Base de Datos
                        Eliga su opcion (del 1 al 2):\s""");
                f.exportarJSON(sc.nextLine());
                break;

            case "7":
                System.out.print("""
                        - 1) Importar toda la Base de Datos
                        - 2) Importar un solo Coche a la Base de Datos
                        Eliga su opcion (del 1 al 2):\s""");
                f.importarJSON(sc.nextLine());
                break;

            case "8":
                f.generarInforme();
                break;

            case "9":
                System.out.println("Gracias por utilizar nuestros servicios, ¡Hasta la Proxima!");
                return;

            default:
                System.out.println("""
                        -------------------------------------------------
                          Por favor, eliga un numero válido del 1 al 9
                        -------------------------------------------------""");
                break;
        }
        menu(f);
    }
}
