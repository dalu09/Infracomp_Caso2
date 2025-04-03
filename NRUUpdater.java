class NRUUpdater extends Thread {
    private final PageTable pageTable;

    public NRUUpdater(PageTable pageTable) {
        this.pageTable = pageTable;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
            }
            try {
                pageTable.resetReferencedBits();
            } catch (Exception ex) {
                System.out.println("Error in resetReferencedBits: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
}
