import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Imagen {
    byte[] header = new byte[54];
    byte[][][] imagen;
    int alto, ancho;
    int padding;

    /***
    * Método para crear una matriz imagen a partir de un archivo.
    * @param input: nombre del archivo. El formato debe ser BMP de 24 bits de bit depth
    * @pos la matriz imagen tiene los valores correspondientes a la imagen
    * almacenada en el archivo.
    * */
    public Imagen(String nombre) {
        try (FileInputStream fis = new FileInputStream(nombre)) {
            fis.read(header);
            
            ancho = ((header[21] & 0xFF) << 24) | ((header[20] & 0xFF) << 16) |
                    ((header[19] & 0xFF) << 8) | (header[18] & 0xFF);
            alto = ((header[25] & 0xFF) << 24) | ((header[24] & 0xFF) << 16) |
                   ((header[23] & 0xFF) << 8) | (header[22] & 0xFF);
            
            System.out.println("Ancho: " + ancho + " px, Alto: " + alto + " px");
            imagen = new byte[alto][ancho][3];
            int rowSizeSinPadding = ancho * 3;
            padding = (4 - (rowSizeSinPadding % 4)) % 4;
            
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    fis.read(pixel);
                    imagen[i][j][0] = pixel[0];
                    imagen[i][j][1] = pixel[1];
                    imagen[i][j][2] = pixel[2];
                }
                fis.skip(padding);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
    * Método para escribir una imagen a un archivo en formato BMP
    * @param output: nombre del archivo donde se almacenará la imagen.
    * Se espera que se invoque para almacenar la imagen modificada.
    * @pre la matriz imagen debe haber sido inicializada con una imagen
    * @pos se creó el archivo en formato bmp con la información de la matriz imagen
    */
    public void escribirImagen(String output) {
        byte pad = 0;
        try (FileOutputStream fos = new FileOutputStream(output)) {
            fos.write(header);
            byte[] pixel = new byte[3];
            for (int i = 0; i < alto; i++) {
                for (int j = 0; j < ancho; j++) {
                    pixel[0] = imagen[i][j][0];
                    pixel[1] = imagen[i][j][1];
                    pixel[2] = imagen[i][j][2];
                    fos.write(pixel);
                }
                for (int k = 0; k < padding; k++) fos.write(pad);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class FiltroSobel {
    Imagen imagenIn;
    Imagen imagenOut;
    
    public FiltroSobel(Imagen imagenEntrada, Imagen imagenSalida) {
        imagenIn = imagenEntrada;
        imagenOut = imagenSalida;
    }
    
    static final int[][] SOBEL_X = {
        {-1, 0, 1},
        {-2, 0, 2},
        {-1, 0, 1}
    };
    
    static final int[][] SOBEL_Y = {
        {-1, -2, -1},
        { 0,  0,  0},
        { 1,  2,  1}
    };
    
    /**
    * Método para para aplicar el filtro de Sobel a una imagen BMP
    * @pre la matriz imagenIn debe haber sido inicializada con una imagen
    * @pos la mstriz imagenOut fue modificada aplicando el filtro Sobel
    */
    public void applySobel() {
        for (int i = 1; i < imagenIn.alto - 1; i++) {
            for (int j = 1; j < imagenIn.ancho - 1; j++) {
                int gradXRed = 0, gradXGreen = 0, gradXBlue = 0;
                int gradYRed = 0, gradYGreen = 0, gradYBlue = 0;
                
                for (int ki = -1; ki <= 1; ki++) {
                    for (int kj = -1; kj <= 1; kj++) {
                        int red = imagenIn.imagen[i + ki][j + kj][0] & 0xFF;
                        int green = imagenIn.imagen[i + ki][j + kj][1] & 0xFF;
                        int blue = imagenIn.imagen[i + ki][j + kj][2] & 0xFF;
                        
                        gradXRed += red * SOBEL_X[ki + 1][kj + 1];
                        gradXGreen += green * SOBEL_X[ki + 1][kj + 1];
                        gradXBlue += blue * SOBEL_X[ki + 1][kj + 1];
                        gradYRed += red * SOBEL_Y[ki + 1][kj + 1];
                        gradYGreen += green * SOBEL_Y[ki + 1][kj + 1];
                        gradYBlue += blue * SOBEL_Y[ki + 1][kj + 1];
                    }
                }
                
                int red = Math.min(Math.max((int) Math.sqrt(gradXRed * gradXRed + gradYRed * gradYRed), 0), 255);
                int green = Math.min(Math.max((int) Math.sqrt(gradXGreen * gradXGreen + gradYGreen * gradYGreen), 0), 255);
                int blue = Math.min(Math.max((int) Math.sqrt(gradXBlue * gradXBlue + gradYBlue * gradYBlue), 0), 255);
                
                imagenOut.imagen[i][j][0] = (byte) red;
                imagenOut.imagen[i][j][1] = (byte) green;
                imagenOut.imagen[i][j][2] = (byte) blue;
            }
        }
    }
}
