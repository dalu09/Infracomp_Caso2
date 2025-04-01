import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Imagen imagenModificada = null;

        while (true) {
            System.out.println("Menú:");
            System.out.println("0. Salir");
            System.out.println("1. Generar referencias");
            System.out.println("2. Calcular hits y fallas de página");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();

            switch (opcion) {
                case 0:
                    System.out.println("Saliendo del programa.");
                    scanner.close();
                    System.exit(0);
                    break;
                case 1:
                    System.out.print("Ingrese el tamaño de página: ");
                    int tamanoPagina = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo BMP: ");
                    String archivoImagen = scanner.next();
                    imagenModificada = new Imagen(archivoImagen);
                    crearReferencias(imagenModificada, tamanoPagina);
                    break;
                case 2:
                    System.out.print("Ingrese el número de marcos de página: ");
                    int numMarcos = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo de referencias: ");
                    String archivoReferencias = scanner.next();
                    simularPaginacion(archivoReferencias, numMarcos);
                    break;
            }
        }
    }

    public static void crearReferencias(Imagen img, int tamanoPagina) throws IOException {
        try (BufferedWriter output = new BufferedWriter(new FileWriter("referencias.txt"))) {
            int numFilas = img.alto;
            int numColumnas = img.ancho;
            int totalBytesImagen = numFilas * numColumnas * 3;
            int totalBytesFiltros = numFilas * numColumnas * 3 * 2; 
            int totalBytesRespuesta = totalBytesFiltros + totalBytesImagen;
    
            int refTotal = (36 * (numFilas - 2) * (numColumnas - 2))*2;
            int paginasTotales = (refTotal + tamanoPagina - 1) / tamanoPagina;
    
            output.write("TP=" + tamanoPagina + "\n");
            output.write("NF=" + numFilas + "\n");
            output.write("NC=" + numColumnas + "\n");
            output.write("NR=" + refTotal + "\n");
            output.write("NP=" + paginasTotales + "\n");
    
            int[] paginaVirtual = {0};  // Usamos un array para que sea modificable dentro del método
            int[] desplaz = {0};
    
            String[] coloresRGB = {"R", "G", "B"};
    
            for (int i = 1; i < numFilas - 1; i++) {
                for (int j = 1; j < numColumnas - 1; j++) {
                    for (int ki = -1; ki <= 1; ki++) {
                        for (int kj = -1; kj <= 1; kj++) {
                            for (String color : coloresRGB) {
                                actualizarPagina(output, "Imagen[" + (i - 1) + "][" + j + "]." + color + ",R", tamanoPagina, paginaVirtual, desplaz);
                            }
    
                            for (String color : coloresRGB) {
                                actualizarPagina(output, "SOBEL_X[" + (i - 1) + "][" + j + "]." + color + ",W", tamanoPagina, paginaVirtual, desplaz);
                            }
    
                            for (String color : coloresRGB) {
                                actualizarPagina(output, "SOBEL_Y[" + (i - 1) + "][" + j + "]." + color + ",W", tamanoPagina, paginaVirtual, desplaz);
                            }
                        }
                    }
    
                    for (String color : coloresRGB) {
                        actualizarPagina(output, "Rta[" + i + "][" + j + "]." + color + ",W", tamanoPagina, paginaVirtual, desplaz);
                    }
                }
            }
        }
    }
    
    
    private static void actualizarPagina(BufferedWriter output, String referencia, int tamanoPagina, int[] paginaVirtual, int[] desplaz) throws IOException {
        output.write(referencia + ", " + paginaVirtual[0] + "," + desplaz[0] + "\n");
        desplaz[0]++;
        if (desplaz[0] == tamanoPagina) {
            desplaz[0] = 0;
            paginaVirtual[0]++;
        }
    }
    
    public static void simularPaginacion(String archivoReferencias, int numMarcos) {
        PageTable pageTable = new PageTable(numMarcos);
        FaultsCounter faultsCounter = new FaultsCounter();
        NRUThread nruThread = new NRUThread(pageTable);
        UpdaterThread updaterThread = new UpdaterThread(pageTable);

        nruThread.start();
        updaterThread.start();

        try (BufferedReader reader = new BufferedReader(new FileReader(archivoReferencias))) {
            String linea;
            int contador = 0;
            while ((linea = reader.readLine()) != null) {
                if (linea.startsWith("Imagen") || linea.startsWith("Filtro") || linea.startsWith("Respuesta")) {
                    String[] partes = linea.split(",");
                    int paginaVirtual = Integer.parseInt(partes[1]);

                    boolean hit = pageTable.loadPage(paginaVirtual);
                    if (hit) {
                        faultsCounter.countHit();
                    } else {
                        faultsCounter.countFault();
                    }
                }
                contador++;
                if (contador % 10000 == 0) {
                    Thread.sleep(1);
                }
            }

            System.out.println("Simulación completada.");
            System.out.println("Total de fallas de página: " + faultsCounter.getFaults());
            System.out.println("Total de hits: " + faultsCounter.getHits());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            nruThread.interrupt();
            updaterThread.interrupt();
            try {
                nruThread.join();
                updaterThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}