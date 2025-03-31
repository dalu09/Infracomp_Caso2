import java.io.*;
import java.util.*;

public class Memoria {
    private Map<Integer, Frame> memoria;
    private int marcosDePagina;
    private int fallasPagina = 0;
    private int hits = 0;
    private long tiempoTotal = 0;
    
    public Memoria(int marcosDePagina) {
        this.marcosDePagina = marcosDePagina;
        this.memoria = new LinkedHashMap<>(marcosDePagina, 0.75f, true);
    }

    public synchronized int accederPagina(int paginaVirtual) {
        if (memoria.containsKey(paginaVirtual)) {
            hits++;
            memoria.get(paginaVirtual).referencia = true;
            tiempoTotal += 50; // Tiempo de acceso RAM
            return memoria.get(paginaVirtual).marco;
        } else {
            fallasPagina++;
            tiempoTotal += 10_000_000; // Tiempo de acceso Swap (10 ms)
            return realizarSwap(paginaVirtual);
        }
    }

    private synchronized int realizarSwap(int paginaVirtual) {
        int marcoReemplazo;
        if (memoria.size() < marcosDePagina) {
            marcoReemplazo = memoria.size();
        } else {
            marcoReemplazo = getMarcoDisponibleSwap();
            memoria.remove(marcoReemplazo);
        }
        memoria.put(paginaVirtual, new Frame(paginaVirtual, marcoReemplazo));
        return marcoReemplazo;
    }

    private synchronized int getMarcoDisponibleSwap() {
        for (Integer marco : memoria.keySet()) {
            if (!memoria.get(marco).referencia) {
                return marco;
            }
        }
        return memoria.keySet().iterator().next();
    }

    public void imprimirResultados() {
        System.out.println("Fallas de página: " + fallasPagina);
        System.out.println("Porcentaje de hits: " + (hits * 100.0 / (hits + fallasPagina)) + "%");
        System.out.println("Tiempo total: " + tiempoTotal / 1_000_000.0 + " ms");
    }
}

class Frame {
    int pagina;
    int marco;
    boolean referencia;
    boolean modificado;
    
    public Frame(int pagina, int marco) {
        this.pagina = pagina;
        this.marco = marco;
        this.referencia = true;
        this.modificado = false;
    }
}