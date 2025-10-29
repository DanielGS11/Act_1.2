import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        FuncionesConcesionario f = new FuncionesConcesionario();

        menu(f);
    }

    private static void menu(FuncionesConcesionario f) {
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

            opcionesMenu(f, sc, ans);
    }

    private static void opcionesMenu(FuncionesConcesionario f, Scanner sc, String ans) {
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
