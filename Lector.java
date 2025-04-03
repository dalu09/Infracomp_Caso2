import java.io.*;

class Lector extends Thread {
    private final PageTable pageTable;
    private final String archivoReferencias;
    private int hits = 0;
    private int fallos = 0;

    public Lector(PageTable pageTable, String archivoReferencias) {
        this.pageTable = pageTable;
        this.archivoReferencias = archivoReferencias;
    }

    @Override
    public void run() {
        System.out.println("Procesador iniciado. Leyendo archivo...");

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias))) {
            String linea;
            int contador = 0;
            while ((linea = reader.readLine()) != null) {
                System.out.println("Leyendo línea: " + linea);
                if (linea.startsWith("Imagen") || linea.startsWith("SOBEL") || linea.startsWith("Rta")) {
                    String[] partes = linea.split(",");

                    int paginaVirtual = Integer.parseInt(partes[1]);

                    boolean hit = pageTable.loadPage(paginaVirtual, partes[3]);
                    if (hit) {
                        hits++;
                        System.out.println("HIT: Página " + paginaVirtual);
                    } else {
                        fallos++;
                        System.out.println("FALLO: Página " + paginaVirtual);
                    }

                    System.out.println("HOAKAKLAA");

                    contador++;
                    if (contador % 10000 == 0) {
                        Thread.sleep(1);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Finalizado. Hits: " + hits + ", Fallos: " + fallos);
    }
}
