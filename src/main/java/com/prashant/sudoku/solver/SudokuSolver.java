package com.prashant.sudoku.solver;

import com.prashant.sudoku.image.ImageManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
/**
 * Created by prashant on 3/7/16.
 */
public class SudokuSolver {

    private static final String IMAGE = "0";
    private static final String TEXT = "1";

    private static Logger logger = LoggerFactory.getLogger(SudokuSolver.class);

    public static void main(String[] args) {

        if (IMAGE.equals(args[0])) solveImage(args[1]);
        else if (TEXT.equals(args[0])) solveTxt(args[1]);
        else logger.info("Invalid first argument : Please use 0 (Image) or 1 (Text)..");

    }

    /**
     * Solve a sudoku in a given file
     */
    public static void solveTxt (String filename) {

        try {
            SolveRoutines.display_grid(filename);

            long startTime = System.currentTimeMillis();
            HashMap<String, String> values = SolveRoutines.parsetxt(filename);

            if (values==null) return;

            SolveRoutines.display_values(SolveRoutines.search(values));

            logger.info("\nTime taken to solve (ms) : {}\n", (System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            logger.error(e.toString());
        }

    }

    /**
     * Solve a sudoku in a given file
     */
    public static void solveImage (String filename) {

        try {
            long startTime = System.currentTimeMillis();

            Sudoku sudoku = ImageManipulator.convertToSudoku(filename, false);
            if (sudoku==null) {
                logger.info("Sorry, we could not identify the Sudoku puzzle from the given image..");
                return;
            }

            SolveRoutines.display_grid_from_object(sudoku);

            HashMap<String, String> values = SolveRoutines.loadSudoku(sudoku);
            if (values==null) {
                logger.info("Sorry, looks like this puzzle can not be solved..");
                return;
            }

            SolveRoutines.display_values(SolveRoutines.search(values));
            logger.info("\nTime taken to solve (ms) : {}\n", (System.currentTimeMillis() - startTime));
        } catch (IOException e) {
            logger.error(e.toString());
        }

    }



}
