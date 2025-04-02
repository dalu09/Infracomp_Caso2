import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        Imagen imagenModificada;

        while (true) {
            System.out.println("Menú:");
            System.out.println("0. Salir");
            System.out.println("1. Generar referencias");
            System.out.println("2. Calcular hits y fallas de página");
            System.out.print("Seleccione una opción: ");
            int opcion = scanner.nextInt();

            switch (opcion) {
                case 0: {
                    System.out.println("Saliendo del programa.");
                    scanner.close();
                    System.exit(0);
                    break;
                }
                case 1: {
                    System.out.print("Ingrese el tamaño de página: ");
                    int tamanoPagina = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo BMP: ");
                    String archivoImagen = scanner.next();
                    imagenModificada = new Imagen(archivoImagen);
                    crearReferencias(imagenModificada, tamanoPagina);
                    break;
                }
                case 2: {
                    System.out.print("Ingrese el número de marcos de página: ");
                    int numMarcos = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo de referencias: ");
                    String archivoReferencias = scanner.next();
                    simularPaginacion(archivoReferencias, numMarcos);
                    break;
                }
            }
        }
    }

    private static void ayuda(BufferedWriter output, String origen, int i, int j, String argAdicional, String accion, int NF, int NC, int tam) throws IOException {
    
        int imagenR = 3 * NF * NC;
        int sobelXR = imagenR + 36;
        int sobelYR = imagenR + sobelXR + 36;
    
        int numPagina, desplazamiento;
    
        Map<String, Integer> rgb = new HashMap<>();
        rgb.put(".r", 0);
        rgb.put(".g", 1);
        rgb.put(".b", 2);
    
        switch (origen) {
            case "Imagen": {
                numPagina = (3 * i + j + rgb.get(argAdicional)) / tam;
                desplazamiento = (3 * i + j + rgb.get(argAdicional)) % tam;
                break;
            }
            case "SOBEL_X": {
                numPagina = (imagenR + 4 * i + j) / tam;
                desplazamiento = (imagenR + 4 * i + j) % tam;
                break;
            }
            case "SOBEL_Y": {
                numPagina = (sobelXR + 4 * i + j) / tam;
                desplazamiento = (sobelXR + 4 * i + j) % tam;
                break;
            }
            case "Rta": {
                numPagina = (sobelYR + 3 * i + j + rgb.get(argAdicional)) / tam;
                desplazamiento = (sobelYR + 3 * i + j + rgb.get(argAdicional)) % tam;
                break;
            }
            default: {
                return;
            }
        }
    
        output.write(origen + "[" + i + "][" + j + "]" + argAdicional + "," + numPagina + "," + desplazamiento + "," + accion + "\n");
    }
    

    public static void crearReferencias(Imagen img, int tamanoPagina) {
        try (BufferedWriter output = new BufferedWriter(new FileWriter("referencias.txt"))) {
            int numFilas = img.alto;
            int numColumnas = img.ancho;
            int refTotal = (numFilas - 2) * (numColumnas - 2) * 81 + (numFilas - 2) * (numColumnas - 2) * 3;
            final int pagsVirtuales = (72 + 6 * numFilas * numColumnas) / tamanoPagina + 1;
            
            output.write("P=" + tamanoPagina + "\n");
            output.write("NF=" + numFilas + "\n");
            output.write("NC=" + numColumnas + "\n");
            output.write("NR=" + refTotal + "\n");
            output.write("NP=" + pagsVirtuales + "\n");
 
    
            for (int i = 1; i < numFilas - 1; i++) {
                for (int j = 1; j < numColumnas - 1; j++) {                    
                    for (int ki = -1; ki <= 1; ki++) {
                        for (int kj = -1; kj <= 1; kj++) {

                            ayuda(output, "Imagen", i + ki, j + kj, ".r", "R", numFilas, numColumnas, tamanoPagina);
                            ayuda(output, "Imagen", i + ki, j + kj, ".g", "R", numFilas, numColumnas, tamanoPagina);
                            ayuda(output, "Imagen", i + ki, j + kj, ".b", "R", numFilas, numColumnas, tamanoPagina);
                    
                            ayuda(output, "SOBEL_X", ki + 1, kj + 1, "", "R", numFilas, numColumnas, tamanoPagina);
                            ayuda(output, "SOBEL_X", ki + 1, kj + 1, "", "R", numFilas, numColumnas, tamanoPagina);
                            ayuda(output, "SOBEL_X", ki + 1, kj + 1, "", "R", numFilas, numColumnas, tamanoPagina);

                            ayuda(output, "SOBEL_Y", ki + 1, kj + 1, "", "R", numFilas, numColumnas, tamanoPagina);
                            ayuda(output, "SOBEL_Y", ki + 1, kj + 1, "", "R", numFilas, numColumnas, tamanoPagina);
                            ayuda(output, "SOBEL_Y", ki + 1, kj + 1, "", "R", numFilas, numColumnas, tamanoPagina);
                        }
                    }
                    
                    ayuda(output, "Rta", i, j, ".r", "W", numFilas, numColumnas, tamanoPagina);
                    ayuda(output, "Rta", i, j, ".g", "W", numFilas, numColumnas, tamanoPagina);
                    ayuda(output, "Rta", i, j, ".b", "W", numFilas, numColumnas, tamanoPagina);
                }
            }
    
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");

        } catch (IOException e) {}
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
                if (linea.startsWith("Imagen") || linea.startsWith("SOBEL") || linea.startsWith("Rta")) {
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
                if (contador % 10000 == 0) Thread.sleep(1);
            }

            System.out.println("Simulación completada!");
            System.out.println("Total de fallas de página: " + faultsCounter.getFaults());
            System.out.println("Total de hits: " + faultsCounter.getHits());

        } catch (IOException | InterruptedException e) {} finally {
            nruThread.interrupt();
            updaterThread.interrupt();
            try {
                nruThread.join();
                updaterThread.join();
            } catch (InterruptedException e) {}
        }
    }
}