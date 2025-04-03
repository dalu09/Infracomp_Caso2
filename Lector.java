import java.io.*;

class Lector extends Thread {
    private final Tabla tabla;
    private final String archivoReferencias;
    private int hits = 0;
    private int fallos = 0;

    public Lector(Tabla tabla, String archivoReferencias) {
        this.tabla = tabla;
        this.archivoReferencias = archivoReferencias;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias))) {
            String linea;
            int contador = 0;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("Imagen") || linea.startsWith("SOBEL") || linea.startsWith("Rta")) {
                    String[] partes = linea.split(",");

                    int pagina = Integer.parseInt(partes[1]);

                    boolean hit = tabla.cargarPagina(pagina, partes[3]);
                    if (hit) {
                        hits++;
                    } else {
                        fallos++;
                    }

                    contador++;
                    if (contador % 10000 == 0) {
                        Thread.sleep(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("# de hits: " + hits + ", # de fallos: " + fallos);
    }
}
