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
        solve("src/main/resources/puzz.txt");
    }

    public static void solve (String filename) {

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


}
