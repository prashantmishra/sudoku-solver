import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by prashant on 7/7/16.
 */
public class ImageManipulator{

    static int width;
    static int height;
    static float[][] intensity;
    static int[][] isWhite;
    static int scanwidth = 20;

    public static void main(String[] foo) {
        for (int i = 1; i < 35; i++) {
            scanwidth = (int)((float) Math.min(width, height) * (((float) i)/100));
            convertImage();
        }
    }

    private static void convertImage () {
        try {
            BufferedImage image = ImageIO.read(new File("resources/img5/img5.png"));
            marchThroughImage(image);
            create();
            File f = new File("resources/img5/img5bw90-" + scanwidth + ".png");
            ImageIO.write(createImage(), "PNG", f);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private static BufferedImage createImage() {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int r = isWhite[i][j] == 0? 255 : 0;
                int g = isWhite[i][j] == 0? 255 : 0;
                int b = isWhite[i][j] == 0? 255 : 0;
                int a = 255;
                int c = (a << 24) | (r << 16) | (g << 8) | b;;
                out.setRGB(j, i, c);
            }
        }
        return out;
    }

    public static void create() {
        isWhite = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int left = (j - scanwidth) < 0 ? 0 : (j - scanwidth);
                int right = (j + scanwidth) >= width ? (width - 1) : (j + scanwidth);
                int up = (i - scanwidth) < 0 ? 0 : (i - scanwidth);
                int down = (i + scanwidth) >= height ? (height - 1) : (i + scanwidth);
                int count = 0;
                float sum = 0;
                for (int k = up; k <= down; k++) {
                    for (int l = left; l <= right; l++) {
                        sum = sum + intensity[k][l];
                        count++;
                    }
                }
                if (intensity[i][j] < 0.9*(((float) sum) / ((float) count))) {
                    isWhite[i][j] = 1;
                } else {
                    isWhite[i][j] = 0;
                }
            }
        }
    }

    public static void printPixelARGB(int pixel) {
        int alpha = (pixel >> 24) & 0xff;
        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;
        System.out.println("pixel: " + pixel + " :: argb: " + alpha + ", " + red + ", " + green + ", " + blue);
    }

    public static void printPixelARGB(Color c) {

        System.out.println("rgb: " + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue());
    }

    private static void marchThroughImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        width = w;
        height = h;
        intensity = new float[height][width];
        System.out.println("width, height: " + w + ", " + h);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                System.out.println("x,y: " + j + ", " + i);
                Color c = new Color(image.getRGB(j, i));
                intensity[i][j] = (c.getRed()+c.getGreen()+c.getBlue())/3;
                printPixelARGB(c);
                System.out.println("");
            }
        }
    }

}
