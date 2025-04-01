import java.io.*;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
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

    public static void crearReferencias(Imagen img, int tamanoPagina) {
        try (BufferedWriter output = new BufferedWriter(new FileWriter("referencias.txt"))) {
            int numFilas = img.alto;
            int numColumnas = img.ancho;
            int totalBytesImagen = numFilas * numColumnas * 3;
            int totalBytesRespuesta = numFilas * numColumnas;
            int totalBytesFiltros = 9 * 4 * 2; // Dos filtros de 3x3 enteros (4 bytes por entero)
            
            int refTotal = (totalBytesImagen + totalBytesRespuesta + totalBytesFiltros)*3;
            int paginasImagen = (totalBytesImagen + tamanoPagina - 1) / tamanoPagina;
            int paginasRespuesta = (totalBytesRespuesta + tamanoPagina - 1) / tamanoPagina;
            int paginasFiltros = (totalBytesFiltros + tamanoPagina - 1) / tamanoPagina;
            int paginasTotales = paginasImagen + paginasRespuesta + paginasFiltros;
            
            // Escribimos información inicial
            output.write("TP=" + tamanoPagina + "\n");
            output.write("NF=" + numFilas + "\n");
            output.write("NC=" + numColumnas + "\n");
            output.write("NR=" + refTotal + "\n");
            output.write("NP=" + paginasTotales + "\n");
            
            int columnaActual = 0;
            String[] coloresRGB = {"R", "G", "B"};
            int desplaz = 0;
            
            // Escribir las primeras 16 referencias
            for (int contador = 0; contador < 16; contador++) {
                String color = coloresRGB[contador % 3];
                if (contador % 3 == 0 && contador != 0) {
                    columnaActual++;
                }
                output.write("Imagen[0][" + columnaActual + "]." + color + ",0," + desplaz + ",R\n");
                desplaz++;
            }
            
            int filaActual = 0;
            int paginaActual = 0;
            int posEnMensaje = 0;
            int pagMensaje = (totalBytesImagen + tamanoPagina - 1) / tamanoPagina;
            
            int contadorImagen = 16;
            int indiceMensaje = 0;
            boolean continuar = false;
            
            // Escribir referencias del mensaje y colores
            while (contadorImagen < refTotal) {
                output.write("Mensaje[" + indiceMensaje + "]," + pagMensaje + "," + posEnMensaje + ",W\n");
                contadorImagen++;
                for (int i = 0; i < 16; i++) {
                    if (continuar) {
                        output.write("Mensaje[" + indiceMensaje + "]," + pagMensaje + "," + posEnMensaje + ",W\n");
                        contadorImagen++;
                        continuar = false;
                    } else {
                        String color = coloresRGB[contadorImagen % 3];
                        if (contadorImagen % 3 == 0) {
                            columnaActual++;
                            if (columnaActual >= img.ancho) {
                                filaActual++;
                                columnaActual = 0;
                            }
                        }
                        output.write("Imagen[" + filaActual + "][" + columnaActual + "]." + color + "," + paginaActual + "," + desplaz + ",R\n");
                        
                        desplaz++;
                        if (desplaz >= tamanoPagina) {
                            paginaActual++;
                            desplaz = 0;
                        }
                        contadorImagen++;
                        continuar = true;
                    }
                }
                
                posEnMensaje++;
                indiceMensaje++;
                
                if (posEnMensaje >= tamanoPagina) {
                    posEnMensaje = 0;
                    pagMensaje++;
                }
                
                // Acceder a los filtros
                output.write("FiltroX,0,0,R\n");
                output.write("FiltroY,0,0,R\n");
                contadorImagen += 2;
                
                // Escribir en la matriz de respuesta
                int offsetResp = (filaActual * numColumnas + columnaActual);
                int pagResp = offsetResp / tamanoPagina;
                output.write("Respuesta[" + filaActual + "][" + columnaActual + "]," + pagResp + "," + (offsetResp % tamanoPagina) + ",W\n");
                contadorImagen++;
            }
            
            System.out.println("Referencias generadas y guardadas en 'referencias.txt'.");
        } catch (IOException e) {
            e.printStackTrace();
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