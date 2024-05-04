package battleship;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class Field {

    // ~ denotes the fog of war: the unknown area on the opponent's
    // field and the yet untouched area on your field.
    public final String FOG = "~";

    // The symbol O denotes a cell with your ship,
    // X denotes that the ship was hit, and M signifies a miss.
    public enum Symbol {O,X,M};
    public final String[] ROW = "ABCDEFGHIJ".split("");
    public final Map<String, Integer> letterNum;
    public final String[] COLUMN = "1,2,3,4,5,6,7,8,9,10".split(",");
    private final String[][] field_matrix = new String[10][10];
    public Field() {
        initialiseField();
        // Use to map letters to corresponding numbers.
        letterNum = IntStream.range(0, ROW.length).boxed().collect(Collectors.toMap(List.of(ROW)::get, i -> i));
    }
    private void initialiseField(){
        for (var myArray: field_matrix) {
            Arrays.fill(myArray, FOG);
        }
    }

    public void displayField(String feature) {
        System.out.print("  " + String.join(" ", COLUMN));
        System.out.println();
        for (int i = 0; i < ROW.length; i++) {
            if (feature.equals("FOG")) {
                System.out.println(ROW[i] + " " + String.join(" ", field_matrix[i]).replace(Symbol.O.name(),FOG));
            } else {
                System.out.println(ROW[i] + " " + String.join(" ", field_matrix[i]));
            }
        }
        //System.out.println();
    }

    // input parts are "A1 A2 A3" for example.
    // we use the letterNum map to get corresponding number for a letter
    // We then use the combination of number representing a letter from the parts and number from the parts
    // to update coordinate in the field. We remap the numbers from 0 to 9  (original 1 to 10). hence -1.
    public void addCoordinates(String coordinates){
        for (var part: coordinates.split(" ")){
            var row = part.substring(0,1);
            var col = Integer.parseInt(part.substring(1));
            field_matrix[letterNum.get(row)][col-1] = Symbol.O.name();
        }

    }
    // For the sake of simplicity; the project does not consider shots to
    // coordinates that are already shot at to be any different.
    // Regardless of whether the coordinate was previously a hit or a miss,
    // you should display You hit a ship! and You missed! again respectively.
    public boolean updateCoordinates(String coordinates) {
        var row = coordinates.substring(0,1);
        var col = Integer.parseInt(coordinates.substring(1));
        var pos = field_matrix[letterNum.get(row)][col - 1];
        if (List.of(Symbol.O.name(), Symbol.X.name()).contains(pos)){
            field_matrix[letterNum.get(row)][col-1] = Symbol.X.name();
            return true;
        } else {
            field_matrix[letterNum.get(row)][col-1] = Symbol.M.name();
            return false;
        }
    }

    // Also, the game rules state that ships cannot be adjacent to each other.
    public boolean isAdjacent(String coordinates) {
        for (var part: coordinates.split(" ")) {
            var row = part.substring(0,1);
            var col = Integer.parseInt(part.substring(1)) - 1;
            //Build the combination that will attempt to capture the boundary or adjacent.
            int[] rowCombi = {letterNum.get(row)-1,letterNum.get(row)+1,letterNum.get(row),letterNum.get(row)+1,
                    letterNum.get(row)-1,letterNum.get(row),letterNum.get(row)+1,letterNum.get(row)-1};
            int[] colCombi = {col-1,col-1,col-1,col+1,col+1,col+1,col,col};
            for(var i=0;i<rowCombi.length;i++) {
                var r = rowCombi[i];
                var c = colCombi[i];
                //check out of boundary here.
                if (r < 0 || r > 9 || c < 0 || c > 9) continue;
                //check for adjacency...
                if (Objects.equals(field_matrix[r][c], Symbol.O.name())) {
                    return true;
                }
            }
        }

        return false;
    }

    public String[] getRow() {
        return ROW;
    }
    public String[] getColumn() {
        return COLUMN;
    }
}
