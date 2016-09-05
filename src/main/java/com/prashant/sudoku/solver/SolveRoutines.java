package com.prashant.sudoku.solver;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by prashant on 3/7/16.
 */
public class SolveRoutines {

    private static Logger logger = LoggerFactory.getLogger(SolveRoutines.class);
    private static final String divider = "|---------|---------|---------|---------|---------|---------|---------|---------|---------|";


    /**
     * Takes a text file and returns a grid containing all the possible values it can contain after applying the constraints
     */
    public static HashMap<String, String> parsetxt(String filename) throws IOException {
        File file = new File(filename);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        int line = 0;
        String str = br.readLine();

        HashMap<String, String> values = new HashMap<>();
        int cell = 0;

        logger.debug("Setting all cell values to \"123456789\"");
        for (String s : SolveHelper.cells) {
            values.put(s, "123456789");
        }

        while (str!=null) {
            line++;
            str.replace(" ","");
            if (str.length()!=9 || line>9) {
                logger.error("Error : Malformed input file. Need 9x9 entries. Exiting now..");
                return null;
            }
            for (char c : str.toCharArray()) {
                cell++;
                if (!Character.isDigit(c)) {
                    logger.error("Error : Malformed input file. Entry [{}] is not a digit. Check file..", SolveHelper.cells.get(cell-1));
                    return null;
                }
                if (c != '0') {
                    logger.debug("Assign value {} to cell {}. Index is {}", String.valueOf(c), SolveHelper.cells.get(cell-1), (cell-1));
                    if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
                    if (!assign(values, SolveHelper.cells.get(cell-1), String.valueOf(c))) return null;
                }
            }
            str = br.readLine();
        }
        br.close();
        fr.close();
        return values;

    }

    /**
     * Takes a Sudoku object and returns a grid containing all the possible values it can contain after applying the constraints
     */
    public static HashMap<String, String> loadSudoku(Sudoku sudoku) throws IOException {

        int[][] _data = sudoku.getData();

        HashMap<String, String> values = new HashMap<>();

        int cell = 0;
        logger.debug("Setting all cell values to \"123456789\"");
        for (String s : SolveHelper.cells) {
            values.put(s, "123456789");
        }

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j <9; j++) {
                cell++;
                if (_data[i][j] != 0) {
                    logger.debug("Assign value {} to cell {}. Index is {}", _data[i][j], SolveHelper.cells.get(cell-1), (cell-1));
                    if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
                    if (!assign(values, SolveHelper.cells.get(cell-1), String.valueOf(_data[i][j]))) return null;
                }
            }
        }
        return values;
    }


    /**
     * Takes a grid, value and cell; tries to assign that value to the cell. <br>
     * If not possible, returns false. If possible, removes all other possible values from that cell using put_constraint method
     */
    public static boolean assign(HashMap<String, String> values, String cell, String value) {
        logger.debug("Assigning value {} to cell [{}]", value, cell);
        if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
        String other_values = values.get(cell).replace(value, "");
        for (char c : other_values.toCharArray()) {
            logger.debug("other_values : ({}). Remove value {} from cell [{}]", other_values, String.valueOf(c), cell);
            if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
            if (!put_constraint(values, cell, String.valueOf(c))) return false;
        }
        return true;
    }

    /**
     * Takes a grid, value and cell; tries to remove that value from the cell. <br>
     * If not possible, returns false. If possible and it's the only remaining value, removes the value from all its peers
     * Check if this value is a possible value in atleast one of the cells in each corresponding units
     */
    private static boolean put_constraint(HashMap<String, String> values, String cell, String value) {
        logger.debug("Removing value {} from cell [{}]", value, cell);
        if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
        if (!values.get(cell).contains(value)) {
            logger.debug("Value already not present..");
            return true;
        }
        values.put(cell, values.get(cell).replace(value, ""));
        if (values.get(cell).isEmpty()) {
            logger.debug("Error cell[{}]: No possible values left. Exiting..", cell);
            return false;
        } else if (values.get(cell).length()==1) {
            for (String peer : SolveHelper.peers.get(cell)) {
                logger.debug("Remove value {} from cell [{}]. Peer of cell [{}]", values.get(cell), peer, cell);
                if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
                if(!put_constraint(values, peer, values.get(cell))) return false;
            }
        }
        for (ArrayList<String> unit : SolveHelper.corrUnits.get(cell)) {
            ArrayList<String> value_places = new ArrayList<>();
            for (String c : unit) {
                if (values.get(c).contains(value)) {
                    value_places.add(c);
                }
            }
            if (value_places.isEmpty()) {
                logger.debug("Error : Corr units for cell [{}]. No possible cell left for a value {}. Exiting..", cell, value);
                return false;
            }
            if (value_places.size()==1) {
                logger.debug("Corr units for cell {}. Only one possible place left. Assign value {} to cell [{}]", cell, value, value_places.get(0));
                if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
                if (!assign(values, value_places.get(0), value)) return false;
            }
        }
        return true;
    }

    /**
     * Find a cell with minimum number of possible values, try each sequentially and see if it leads to a solution
     */
    public static HashMap<String, String> search(HashMap<String, String> values) {
        logger.debug("Solving puzzle further..");
        if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
        String min_cell = null;
        boolean unsolved = false;
        for (String cell : SolveHelper.cells) {
            if (values.get(cell).length()!=1) {
                min_cell = min_cell==null? cell : (values.get(cell).length() < values.get(min_cell).length()? cell : min_cell);
                unsolved = true;
            }
        }
        if (!unsolved) {
            logger.info("Looks like we solved it!");
            return values;
        } else {
            for (char c : values.get(min_cell).toCharArray()) {
                HashMap<String, String> values_copy = new HashMap<>(values);
                logger.debug("Assign value {} to cell (min_cell) [{}]", String.valueOf(c), min_cell);
                if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
                if (!assign(values_copy, min_cell, String.valueOf(c))) continue;
                logger.debug("Further solve grid..");
                if (logger.isDebugEnabled()) SolveRoutines.display_values(values);
                if ((values_copy = (search(values_copy)))!=null) {
                    return values_copy;
                }
            }
        }
        return null;
    }

    /**
     * Take a grid and display it as a 9x9 matrix
     */
    public static void display_values(HashMap<String, String> values) {
        if (values==null) {
            logger.info("Sorry, looks like this puzzle can not be solved..");
            return;
        }
        StringBuilder sb = new StringBuilder("Current state of possible values :\n").append(divider).append("\n|");
        int i = 0;
        for (String cell : SolveHelper.cells) {
            if (i%9==0 && i!=0) sb.append("\n").append(divider).append("\n|");
            i++;
            sb.append(values.get(cell)==null? "   NaN   |" : String.format("%1$9s|", StringUtils.center(values.get(cell), 9)));
        }
        sb.append("\n").append(divider);
        logger.info(sb.toString());
    }

    /**
     * Read a grid from file and display it as a 9x9 matrix
     */
    public static void display_grid(String filename) throws IOException{
        File file = new File(filename);
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder("Grid to solve (").append(filename).append(") :\n").append(divider);

        String line = br.readLine();

        while (line!=null) {
            sb.append("\n|");
            for (char c : line.replace(" ","").toCharArray()) {
                sb.append(String.format("%1$9s|", StringUtils.center(c!='0'? String.valueOf(c) : "", 9)));
            }
            sb.append("\n").append(divider);
            line = br.readLine();
        }
        br.close();
        fr.close();
        logger.info(sb.toString());
    }

    /**
     * Read a grid from Sudoku object and display it as a 9x9 matrix
     */
    public static void display_grid_from_object(Sudoku s) throws IOException{

        StringBuilder sb = new StringBuilder("Grid to solve :\n").append(divider);
        int[][] data = s.getData();

        for (int i = 0; i < 9; i++) {
            sb.append("\n|");
            for (int j = 0; j < 9; j++) {
                sb.append(String.format("%1$9s|", StringUtils.center(data[i][j]!=0? String.valueOf(data[i][j]) : "", 9)));
            }
            sb.append("\n").append(divider);
        }
        logger.info(sb.toString());
    }


}
