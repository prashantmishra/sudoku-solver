package com.prashant.sudoku.image

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by prashant on 29/8/16.
 */
public class Classifier {

    static Logger log = LoggerFactory.getLogger(Classifier.class);

    private static Map<Integer, int[][]> data;
    private static boolean loaded;

    /**
     * Load the training data for nearest neighbor comparision
     */
    private static void loadData() throws Exception {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        data = new HashMap<>();

        data.put(0, new int[25][25]);
        for (int i = 1; i <= 9; i++) {
            for (int j = 1; j <= 4; j++) {
                Mat s = Imgcodecs.imread("resources/training_data/" + i + "_" + j + ".png", 0);
                if (s.size().height!=25 || s.size().width!=25) throw new Exception("Need a 25x25 image for training..");
                int[][] _data = new int[25][25];
                for (int x = 0; x < 25; x++) {
                    for (int y = 0; y < 25; y++){
                        _data[x][y] = (int) s.get(y,x)[0];
                    }
                }
                data.put(i*100 + j, _data);
            }
        }
    }

    /**
     * Compare the given mat image and return the nearest matching number
     */
    public static int getBox(Mat image) throws Exception{
        if (data==null) loadData();

        if (image.size().height!=25 || image.size().width!=25) throw new Exception("Need a 25x25 image for identification..");
        int max_match = 0;
        int max_match_count = 0;
        for (Integer i : data.keySet()) {
            int[][] _data = data.get(i);

            int count = 0;

            for (int x = 0; x < 25; x++) {
                for (int y = 0; y < 25; y++){
                    if ((int) image.get(y,x)[0] == _data[x][y]) count++;
                }
            }

            if (count > max_match_count) {
                max_match = i/100;
                max_match_count = count;
            }
        }
        return max_match;
    }
}
