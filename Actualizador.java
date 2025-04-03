class Actualizador extends Thread {
    private final Tabla tabla;

    public Actualizador(Tabla tabla) {
        this.tabla = tabla;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
            try {
                tabla.resetear();
            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
}
