package com.prashant.sudoku.solver;

import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import org.slf4j.Logger;
/**
 * Created by prashant on 3/7/16.
 */
public class SudokuSolver {

    private static Logger logger = LoggerFactory.getLogger(SudokuSolver.class);

    public static void main(String[] args) {
//        solveTxt("src/main/resources/puzz.txt");
        solveImage("resources/img-1.png");
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
