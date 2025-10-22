import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Concesionario {
    private List<Coche> coches = new ArrayList<>();
    private List<Path> rutasFicheros = new ArrayList<>();

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


}
