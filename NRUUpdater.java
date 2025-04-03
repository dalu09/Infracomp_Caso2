class NRUUpdater extends Thread {
    private final Tabla tabla;

    public NRUUpdater(Tabla tabla) {
        this.tabla = tabla;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException ex) {
            }
            try {
                tabla.resetReferencedBits();
            } catch (Exception ex) {
                System.out.println("Error in resetReferencedBits: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
    
}
