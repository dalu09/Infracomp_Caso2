public class NRUThread extends Thread {
    private PageTable pageTable;

    public NRUThread(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (pageTable) {
                    pageTable.categorizePages();
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
        }
    }
}
