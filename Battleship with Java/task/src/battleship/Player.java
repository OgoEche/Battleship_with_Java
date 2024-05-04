package battleship;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class Player {
    private Field field;
    public enum Stage {LOAD, PLAY,}
    private final String[] ship_names = "Aircraft Carrier,Battleship,Submarine,Cruiser,Destroyer".split(",");
    private final int[] ship_cells = {5,4,3,3,2};
    private List<Ship> fleet = new ArrayList<Ship>();
    private final UserInput input;
    private final int id;
    //Use to link parts/coordinate occupied by a ship in the field to a ship object
    private Map<String,Ship> parts2ShipMap = new HashMap<String,Ship>();
    public record PlayerRecord(Field myField, List<Ship> myFleet, Map<String,Ship> parts2Ship){};
    PlayerRecord playerRecord;
    public Player(int id){
        this.id = id;
        field = new Field();
        input = new UserInput();
        buildFleet();
    }
    private void setPlayerRecord(){
        playerRecord = new PlayerRecord(field, fleet, parts2ShipMap);
    }
    public PlayerRecord getPlayerRecord(){
        return playerRecord;
    }

    public void swapPlayItems(Field myField, List<Ship> myFleet, Map<String,Ship> parts2Ship) {
        field = myField;
        fleet = myFleet;
        parts2ShipMap = parts2Ship;
    }

    public String toString(){
        return "%s %d".formatted(this.getClass().getSimpleName(), this.id);
    }

    private void buildFleet() {
        for (int v=0;v < ship_names.length; v++) {
            fleet.add(new Ship(ship_names[v], ship_cells[v]));
        }
    }

    //All the parts that a ship occupies points to that ship reference - key[parts] -> value[ship ref]
    public void build_parts_to_ship(String parts, Ship ship) {
        for (var part: parts.split(" ")) {
            parts2ShipMap.put(part, ship);
        }

    }
    // Enter and load fleet into battlefield
    public void loadFleet() {
        System.out.println(this + ", place your ships on the game field");
        var response = " ";
        for (var ship: fleet) {
            field.displayField("NORM");
            System.out.printf("Enter the coordinates of the %s (%d cells):%n", ship.shipName(),ship.cells());
            while (!(response = input.prompt()).isEmpty()) {
                var isValid = validateInput(response,Stage.LOAD,ship);
                if (isValid){
                    var parts = getParts(response);
                    field.addCoordinates(parts);
                    build_parts_to_ship(parts, ship);
                    break;
                }
            }
        }
        field.displayField("NORM");
        setPlayerRecord();
    }

    public void displayPlayerField() {
        // this should point to the opponent's after the swap
        field.displayField("FOG");
        System.out.println("-".repeat(20));
        // this should point to your own field stored in record
        playerRecord.myField().displayField("NORM");
    }

    //I think I should use this method for swapping facets/resource for players
    //What does passing the move means in terms of resources?
    //which will clear the screen.
    public void afterEachMove(){
        var response = " ";
        System.out.println("Press Enter and pass the move to another player");
        response = input.prompt();

    }

    public boolean play(String feature) {
        var response = " ";
        System.out.println(this + ", it's your turn:");
        //activate fog of war here
        //field.displayField("FOG");
        //System.out.println("Take a shot!");
        while (!(response = input.prompt()).isEmpty()) {
            var isValid = validateInput(response, Stage.PLAY);
            if (isValid) {
                var status = field.updateCoordinates(response);
                //activate fog of war here
                System.out.println( status ? "You hit a ship!:" : "You missed!");
                //field.displayField("FOG");
                // display additional normal grid here for debugging
                //if (feature.equals("FOG")) field.displayField("NORM");
                // check the status of the ships and fleet
                if (status) {
                    if (checkShipStatus(response)) {
                        System.out.println("You sank a ship! Specify a new target:");

                    }
                    if (fleet.isEmpty()) {
                        System.out.println("You sank the last ship. You won. Congratulations!");
                        return true;
                    }

                }
                return false;
            }

        }
        return false;
    }

    //Check fleet status by reflecting hit to ships in the field to their corresponding hit variable
    //inside the ship object that form the value in the parts2Ship map
    //Hence, we use the response to update the field, and also to return the ship object value and increment the hit.
    //When the hit in a given ship object is equal to the length of cells, then that ship is completely sunk.
    //We continue until all the ships are sunk.
    public boolean checkShipStatus(String hitResponse) {
        var ship_hit = parts2ShipMap.get(hitResponse);
        ship_hit.incrementHit();
        if (ship_hit.isCellsEqualHit() ) {
            //once the total parts of a ship is hit, which means the ship is sunk, remove from fleet
            fleet.remove(ship_hit);
            return true;
        }
        return false;
    }

    public boolean validateInput(String input, Stage stage, Ship... ship) {
        var value = input.split(" ");
        // use to check whether coordinates are out of bounds
        // Ok, just defining this Functional Interface to avoid having to duplicate the code it points to
        // since we will have stages -LOAD, PLAY, of the game that uses validation differently.
        Function<String[], Boolean> checkBoundary = (bound) -> {
            for (var val : bound) {
                var isRow = Stream.of(field.getRow()).noneMatch(val.substring(0, 1)::equals);
                var isCol = Stream.of(field.getColumn()).noneMatch(val.substring(1)::equals);
                if (isCol || isRow) {
                    System.out.println("Error! You entered the wrong coordinates! Try again:");
                    return true;
                }
            }
            return false;
        };

        //Using enum and switch to call the correct validation during the stages of the game defined in enum Stage.
        switch(stage) {
            // use Functional Interface to check whether coordinates are out of bounds
            case PLAY -> { return !checkBoundary.apply(value);}
            case LOAD -> {

                // use Functional Interface to check whether coordinates are out of bounds
                if (checkBoundary.apply(value)) return false;

                // use to check whether coordinates are not on the same line
                var isRowSame = !value[0].substring(0, 1).equals(value[1].substring(0, 1));
                var isColSame = !value[0].substring(1).equals(value[1].substring(1));
                if (isColSame && isRowSame) {
                    System.out.println("Error! Wrong ship location! Try again:");
                    return false;
                }

                // check whether the user has entered coordinates in such a way that
                // the length of the created ship does not match the expected length
                if (getParts(input).split(" ").length != ship[0].cells()) {
                    System.out.printf("Error! Wrong length of the %s! Try again:%n", ship[0].shipName());
                    return false;
                }

                //Also, the game rules state that ships cannot be adjacent to each other
                if (field.isAdjacent(input)) {
                    System.out.println("Error! You placed it too close to another one. Try again:");
                    return false;
                }
            }

        }
        return true;
    }

    //To get the parts, we use the values in the string to generate a range of letter characters and numbers
    //in ascending or descending order depending on input.
    //Stream.iterate(start, c -> c != end+signed, c -> (c <= end+signed ? ++c : --c)).toList()
    public String getParts(String input) {
        var value = input.split(" ");

        // Get the row/letter from the string and process parts
        var startK = value[0].charAt(0);
        var endK = value[1].charAt(0);
        var signedK = startK < endK ? 1 : -1;
        var letters = Stream.iterate(startK, c -> c != endK+signedK, c -> (c <= endK+signedK ? ++c : --c)).toList();

        // Get the column/number from the string and process parts
        var startV = Integer.parseInt(value[0].substring(1));
        var endV = Integer.parseInt(value[1].substring(1));
        var signed = startV < endV ? 1 : -1;
        var numbers = Stream.iterate(startV, v -> v != endV+signed, v -> v+signed).toList();

        // Combine letters and numbers to build parts
        var parts = new LinkedList<String>();
        for (var i: letters) {
            for (var n: numbers) {
                parts.add(i+""+n);
            }
        }
        return String.join(" ", parts);
    }



}
