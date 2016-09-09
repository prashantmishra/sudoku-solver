package com.prashant.sudoku.image;

import com.prashant.sudoku.solver.Sudoku;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;

import static org.opencv.core.Core.line;
import static org.opencv.core.CvType.CV_8UC1;
import static org.opencv.imgproc.Imgproc.*;

/**
 * Created by prashant on 28/8/16.
 */
public class ImageManipulator {

    static {
        nu.pattern.OpenCV.loadShared();
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    static Logger log = LoggerFactory.getLogger(ImageManipulator.class);

    /**
     * Takes an image file and creates a Sudoku object
     */
    public static Sudoku convertToSudoku(String filename, boolean writeCells) {

        Sudoku _sudoku = null;

        try {

            Mat m = loadAndPreprocess(filename);
            Mat backup = m.clone();
            Object[] res = floodFillGrid(m);
            Mat n = (Mat) res[0];

            Mat warped = warpImage(m, detectEdges(n));

            Mat outer = new Mat((int) backup.size().height + 2, (int) backup.size().width + 2, CV_8UC1, new Scalar(0));
            floodFill(backup, outer, (Point) res[1], new Scalar(0));

            Mat _warped = warpImage(backup, detectEdges(n));
            _sudoku = extractCells(_warped, writeCells);

        } catch (Exception e) {
            log.info("Error : " + e.toString());
        }

        return _sudoku;

    }

    /**
     * Takes a binary preprocessed image, extracts the cells, identifies them and returns a Sudoku object
     */
    private static Sudoku extractCells (Mat image, boolean writeCells) throws Exception {

        double width = image.size().width;
        double height = image.size().height;

        int[][] _data = new int[9][9];

        for (double y = 1; y <= 9; y++) {
            for (double x = 1; x <= 9; x++) {

                Mat src_mat=new Mat(4,1,CvType.CV_32FC2);
                Mat dst_mat=new Mat(4,1,CvType.CV_32FC2);

                src_mat.put(0, 0, (x-1)*width/9, (y-1)*height/9, x*width/9, (y-1)*height/9, (x-1)*width/9, y*height/9, x*width/9, y*height/9);
                dst_mat.put(0, 0, 0, 0, width/9, 0, 0, width/9, width/9, width/9);

                Mat dst=image.clone();
                Mat perspectiveTransform=Imgproc.getPerspectiveTransform(src_mat, dst_mat);
                Imgproc.warpPerspective(image, dst, perspectiveTransform, new Size(width/9, width/9));
                threshold(dst, dst, 128, 255, THRESH_BINARY);

                dst = (Mat)floodFillGrid(dst)[0];
                dst = crudeBoundaryDetection(dst);

                Mat src_mat_f=new Mat(4,1,CvType.CV_32FC2);
                Mat dst_mat_f=new Mat(4,1,CvType.CV_32FC2);

                src_mat_f.put(0, 0, 0, 0, dst.size().width-1, 0, 0, dst.size().height-1, dst.size().width-1, dst.size().height-1);
                dst_mat_f.put(0, 0, 0, 0, 24, 0, 0, 24, 24, 24);

                Mat dst_f=dst.clone();
                Mat perspectiveTransform_f=Imgproc.getPerspectiveTransform(src_mat_f, dst_mat_f);
                Imgproc.warpPerspective(dst, dst_f, perspectiveTransform_f, new Size(25, 25));
                threshold(dst_f, dst_f, 128, 255, THRESH_BINARY);

                if (writeCells) Mat2BufferedImage(dst_f, String.valueOf(10*x+y));

                _data[(int) (y-1)][(int) (x-1)] = Classifier.getBox(dst_f);
            }
        }

        Sudoku s = new Sudoku();
        s.setData(_data);

        return s;
    }

    /**
     * Takes a Mat object and writes a PNG image
     */
    private static void Mat2BufferedImage(Mat m, String s){
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if ( m.channels() > 1 ) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = m.channels()*m.cols()*m.rows();
        byte [] b = new byte[bufferSize];
        m.get(0,0,b);
        BufferedImage image = new BufferedImage(m.cols(),m.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(b, 0, targetPixels, 0, b.length);
        File f = new File("resources/img-" + s + ".png");
        try {
            ImageIO.write(image, "PNG", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Takes a file, loads it, applies adaptive thresholding, inverts the color and returns the Mat image
     */
    private static Mat loadAndPreprocess(String file) {
        Mat s = Highgui.imread(file, 0);
        Mat outerBox = new Mat(s.size(), CV_8UC1);

        int blockSize = (int) (s.size().height * 0.05d);
        blockSize = blockSize%2==0 ? blockSize+1 : blockSize;

        adaptiveThreshold(s, outerBox, 255, ADAPTIVE_THRESH_MEAN_C, THRESH_BINARY, blockSize, 10);
        Core.bitwise_not(outerBox, outerBox);

        return outerBox;
    }

    /**
     * Moving from the corners detects the first pixel which is white, and assumes them to be the location of the four edges
     */
    private static Point[] detectEdges(Mat image) {

        int width = (int) image.size().width;
        int height = (int) image.size().height;

        Point uLeft = new Point();
        Point uRight = new Point();
        Point lLeft = new Point();
        Point lRight = new Point();

        outer:
        for (int i = 0; i < width; i++) {
            for (int y = 0, x = i; x >= 0 && y < height; x--, y++) {
                if (image.get(y, x)[0] == 255) {
                    uLeft.x = x;
                    uLeft.y = y;
                    break outer;
                }
            }
        }

        outer:
        for (int i = width; i >= 0; i--) {
            for (int y = 0, x = i; x < width && y < height; x++, y++) {
                if (image.get(y, x)[0] == 255) {
                    uRight.x = x;
                    uRight.y = y;
                    break outer;
                }
            }
        }

        outer:
        for (int i = 0; i < width; i++) {
            for (int y = height-1, x = i; x >= 0 && y >=0; x--, y--) {
                if (image.get(y, x)[0] == 255) {
                    lLeft.x = x;
                    lLeft.y = y;
                    break outer;
                }
            }
        }

        outer:
        for (int i = width; i >= 0; i--) {
            for (int y = height-1, x = i; x < width && y >=0; x++, y--) {
                if (image.get(y, x)[0] == 255) {
                    lRight.x = x;
                    lRight.y = y;
                    break outer;
                }
            }
        }

        return new Point[]{uLeft, uRight, lLeft, lRight};

    }

    /**
     * Method to find the smallest rectangular area which contains all the white pixels
     */
    private static Mat crudeBoundaryDetection (Mat image) {

        int height = (int) image.size().height;
        int width = (int) image.size().width;

        int yLow = height;
        int xLow = width;
        int yHigh = 0;
        int xHigh = 0;


        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (image.get(y, x)[0] == 255) {
                    if (y<yLow) yLow = y;
                    if (y>yHigh) yHigh = y;
                    if (x<xLow) xLow = x;
                    if (x>xHigh) xHigh = x;
                }
            }
        }

        if (xHigh-xLow<3 || yHigh-yLow<3) return image;

        return new Mat(image, new Rect(new Point(xLow, yLow), new Point(xHigh, yHigh)));

    }

    /**
     * Apply Hough transformation and detect lines
     */
    private static void drawLines(Mat image) {

        int height = (int) image.size().height;
        int width = (int) image.size().width;

        Mat lines = new Mat();
        Imgproc.HoughLines(image, lines, 1, Math.PI/180, height/2);

        for (int i = 0; i < lines.rows(); i++){
            double[] vec = lines.get(i, 0);
            if(vec[1]!=0) {
                double m = -1/Math.tan(vec[1]);
                double c = vec[0]/Math.sin(vec[1]);
                line(image, new Point(0, c), new Point(width, m*width+c), new Scalar(128));
            } else {
                line(image, new Point(vec[0], 0), new Point(vec[0], height), new Scalar(128));
            }
        }

        for (int i = 0; i < lines.cols(); i++){
            double[] vec = lines.get(0, i);
            if(vec[1]!=0) {
                double m =-1/Math.tan(vec[1]);
                double c = vec[0]/Math.sin(vec[1]);
                line(image, new Point(0, c), new Point(width, m*width+c), new Scalar(128));
            } else {
                line(image, new Point(vec[0], 0), new Point(vec[0], height), new Scalar(128));
            }
        }

    }

    /**
     * Warp the input image assuming the four input points to be the edges
     */
    private static Mat warpImage (Mat image, Point[] points) {

        int maxEdge = maxEdge(points);

        Mat src_mat=new Mat(4,1,CvType.CV_32FC2);
        Mat dst_mat=new Mat(4,1,CvType.CV_32FC2);

        src_mat.put(0, 0, points[0].x, points[0].y, points[1].x, points[1].y, points[2].x, points[2].y, points[3].x, points[3].y);
        dst_mat.put(0, 0, 0, 0, maxEdge-1, 0, 0, maxEdge-1, maxEdge-1, maxEdge-1);

        Mat dst=image.clone();
        Mat perspectiveTransform=Imgproc.getPerspectiveTransform(src_mat, dst_mat);
        Imgproc.warpPerspective(image, dst, perspectiveTransform, new Size(maxEdge, maxEdge));

        return dst;

    }

    /**
     * Find the length of the longest edge given four edge points
     */
    private static int maxEdge (Point[] points) {

        double max = 0;

        double edge = (points[0].x - points[1].x)*(points[0].x - points[1].x) + (points[0].y - points[1].y)*(points[0].y - points[1].y);
        max = edge > max ? edge : max;

        edge = (points[2].x - points[3].x)*(points[2].x - points[3].x) + (points[2].y - points[3].y)*(points[2].y - points[3].y);
        max = edge > max ? edge : max;

        edge = (points[0].x - points[2].x)*(points[0].x - points[2].x) + (points[0].y - points[2].y)*(points[0].y - points[2].y);
        max = edge > max ? edge : max;

        edge = (points[1].x - points[3].x)*(points[1].x - points[3].x) + (points[1].y - points[3].y)*(points[1].y - points[3].y);
        max = edge > max ? edge : max;

        return (int) Math.sqrt(max);

    }

    /**
     * Given a binary image, find the largest connected area of white pixels and make all other region black
     */
    private static Object[] floodFillGrid (Mat image) {

        int height = (int) image.size().height;
        int width = (int) image.size().width;

        Mat filledImage = image.clone();

        int max = -1;
        Point maxPt = null;
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (filledImage.get(x, y)[0] >= 128) {
                    Mat outer = new Mat(height+2, width+2, CV_8UC1, new Scalar(0));
                    int area = floodFill(filledImage, outer, new Point(y, x), new Scalar(64));
                    outer = null;
                    if (area > max) {
                        maxPt = new Point(y, x);
                        max = area;
                    }
                }
            }
        }

        if (maxPt==null) return new Object[]{image.clone(), new Point(0, 0)};

        Mat outer = new Mat(height+2, width+2, CV_8UC1, new Scalar(0));
        floodFill(filledImage, outer, maxPt, new Scalar(255));

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                if (filledImage.get(x, y)[0] == 64) {
                    outer = new Mat(height+2, width+2, CV_8UC1, new Scalar(0));
                    floodFill(filledImage, outer, new Point(y, x), new Scalar(0));
                }
            }
        }

        return new Object[]{filledImage, maxPt};

    }

}
