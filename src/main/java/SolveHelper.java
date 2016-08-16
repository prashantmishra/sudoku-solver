import java.util.*;

/**
 * Class to generate a few useful objects for solving sudoku. <br>
 * @author Prashant Mishra
 */
public class SolveHelper {

    protected static ArrayList<String> rows = new ArrayList<>(Arrays.asList(new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I"}));
    protected static ArrayList<String> cols = new ArrayList<>(Arrays.asList(new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9"}));
    protected static ArrayList<String> cells = makeCells(rows, cols);
    protected static ArrayList<ArrayList<String>> units = makeUnits(rows, cols);
    protected static HashMap<String, ArrayList<ArrayList<String>> > corrUnits = makeCorrUnits(cells, units);
    protected static HashMap<String, ArrayList<String>> peers = makePeers(corrUnits);

    // Returns a list of 81 cells
    static ArrayList<String> makeCells (ArrayList<String> rows, ArrayList<String> cols) {
        ArrayList<String> cells = new ArrayList<>();
        for (String s1 : rows) {
            for (String s2 : cols) {
                cells.add(s1 + s2);
            }
        }
        return cells;
    }

    // Returns a list of 27 units : 9 rows, 9 columns, 9 square boxes
    private static ArrayList<ArrayList<String>> makeUnits(ArrayList<String> rows, ArrayList<String> cols) {
        ArrayList<ArrayList<String>> units = new ArrayList<ArrayList<String>>();

        // For rows
        for (String s1 : rows) {
            ArrayList<String> unit = new ArrayList<>();
            for (String s2 : cols) {
                unit.add(s1 + s2);
            }
            units.add(unit);
        }

        // For columns
        for (String s1 : cols) {
            ArrayList<String> unit = new ArrayList<>();
            for (String s2 : rows) {
                unit.add(s2 + s1);
            }
            units.add(unit);
        }

        // For squares
        ArrayList<ArrayList<String>> sRows = new ArrayList<ArrayList<String>>();
        ArrayList<ArrayList<String>> sCols = new ArrayList<ArrayList<String>>();
        sRows.add(new ArrayList<String>(Arrays.asList("A", "B", "C")));
        sRows.add(new ArrayList<String>(Arrays.asList("D", "E", "F")));
        sRows.add(new ArrayList<String>(Arrays.asList("G", "H", "I")));
        sCols.add(new ArrayList<String>(Arrays.asList("1", "2", "3")));
        sCols.add(new ArrayList<String>(Arrays.asList("4", "5", "6")));
        sCols.add(new ArrayList<String>(Arrays.asList("7", "8", "9")));

        for (ArrayList<String> row : sRows) {
            for (ArrayList<String> col : sCols) {
                units.add(makeCells(row, col));
            }
        }

        return units;

    }

    // Returns corresponding 3 units. Ex for C2 : Returns the row, column and the square it belongs to
    private static HashMap<String, ArrayList<ArrayList<String>> > makeCorrUnits(ArrayList<String> cells, ArrayList<ArrayList<String>> units) {
        HashMap<String, ArrayList<ArrayList<String>> > corrUnits = new HashMap<>();
        for (String cell : cells) {
            ArrayList<ArrayList<String>> corrUnit = new ArrayList<>();
            for (ArrayList<String> unit : units) {
                if (unit.contains(cell)) {
                    corrUnit.add(unit);
                }
            }
            corrUnits.put(cell, corrUnit);
        }
        return  corrUnits;
    }

    // Returns the other 20 cells in the corresponding units
    private static HashMap<String,ArrayList<String>> makePeers(HashMap<String, ArrayList<ArrayList<String>>> corrUnits) {
        HashMap<String, ArrayList<String>> peers = new HashMap<>();
        for (String cell : corrUnits.keySet()) {
            HashSet<String> set = new HashSet<>();
            for (ArrayList<String> corrUnit : corrUnits.get(cell)) {
                set.addAll(corrUnit);
            }
            set.remove(cell);
            peers.put(cell, new ArrayList<String>(set));
        }
        return peers;
    }


}
