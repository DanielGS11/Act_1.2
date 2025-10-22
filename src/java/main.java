public class main {
    public static void main(String[] args) {
        Concesionario c = new Concesionario();
        c.cargarPaths().forEach(System.out::println);
    }
}
