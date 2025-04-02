import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UpdaterThread extends Thread {
    private PageTable pageTable;
    private String refFile;
    private long tiempoTotal = 0;
    private int NR = 0;

    public UpdaterThread(PageTable pageTable, String refFile) {
        this.pageTable = pageTable;
        this.refFile = refFile;
    }

    public void run() {
        if (refFile == null || refFile.isEmpty()) {
            System.err.println("Error: La ruta del archivo de referencias no puede ser null o vacía.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(refFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (Thread.currentThread().isInterrupted()) {
                    break; // Sale del bucle si el hilo ha sido interrumpido
                }
    
                line = line.trim();
                if (line.contains("=")) {
                    continue;
                }
    
                String[] partes = line.split(",");
                if (partes.length < 2) {
                    System.err.println("Formato incorrecto en línea: " + line);
                    continue;
                }
    
                try {
                    int page = Integer.parseInt(partes[1].trim()); // Número de página
                    synchronized (pageTable) {
                        boolean hit = pageTable.loadPage(page);
                        tiempoTotal += hit ? 50 : 10_000_000;
                    }
                    NR++;
                    Thread.sleep(1); 
                } catch (NumberFormatException e) {
                    System.err.println("Error al leer la página en línea: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); 
        }
    }       

    public int getNR() {
        return NR;
    }

    public long getTiempoTotal() {
        return tiempoTotal;
    }
}
