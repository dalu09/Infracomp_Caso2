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
        int sobelYR = sobelXR + 36;
    
        int numPagina, desplazamiento, nBytes;
    
        Map<String, Integer> rgb = new HashMap<>();
        rgb.put(".r", 0);
        rgb.put(".g", 1);
        rgb.put(".b", 2);
    
        switch (origen) {
            case "Imagen": {
                nBytes = 3 * i * NC + 3 * j + rgb.get(argAdicional);
                numPagina = nBytes / tam;
                desplazamiento = nBytes % tam;
                break;
            }
            case "SOBEL_X": {
                nBytes = imagenR + 4 * 3 * i + 4 * j;
                numPagina = nBytes / tam;
                desplazamiento = nBytes % tam;
                break;
            }
            case "SOBEL_Y": {
                nBytes = sobelXR + 4 * 3* i + 4 * j;
                numPagina = nBytes / tam;
                desplazamiento = nBytes % tam;
                break;
            }
            case "Rta": {
                nBytes = sobelYR + 3 * NC * i + 3 * j + rgb.get(argAdicional);
                numPagina = nBytes / tam;
                desplazamiento = nBytes % tam;
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
        Tabla tabla = new Tabla(numMarcos);

        Lector lector = new Lector(tabla, archivoReferencias);
        Actualizador actualizador = new Actualizador(tabla);

        lector.start();
        actualizador.start();

        try {
            lector.join();
            actualizador.interrupt();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}