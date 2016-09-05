import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by prashant on 29/8/16.
 */
public class Sudoku {

    Logger logger = LoggerFactory.getLogger(Sudoku.class);

    private int[][] data;

    public void setData(int[][] data) {
        if (data.length!=9 || data[0].length!=9) {
            logger.debug("Not a valid sudoku (9x9). Returning..");
            return;
        }
        this.data = data;
    }

    public int[][] getData() {
        return data;
    }

    public void display() {

    }

}
