import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class UpdaterThread extends Thread {
    private PageTable pageTable;
    private String refFile;
    private long tiempoTotal = 0;
    private int NR = 0;

    public UpdaterThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try (BufferedReader br = new BufferedReader(new FileReader(refFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                int page = Integer.parseInt(line.trim());
                synchronized (pageTable) {
                    boolean hit = pageTable.loadPage(page);
                    tiempoTotal += hit ? 50 : 10_000_000;
                }
                NR++;
                Thread.sleep(1);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getNR() {
        return NR;
    }

    public long getTiempoTotal() {
        return tiempoTotal;
    }
}
