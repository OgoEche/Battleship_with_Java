package battleship;

public class Ship {
    private final String shipName;
    private final int cells;
    private int hit = 0;
    public Ship(String shipName, int cells) {
        this.shipName = shipName;
        this.cells = cells;
    }
    public String shipName() { return shipName;}
    public int cells() { return cells;}

    public boolean isCellsEqualHit() {
        return hit == cells;
    }
    public boolean incrementHit() {
        if ( hit < cells) {
            hit++;
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "%s<%s,%d:%d>".formatted(this.getClass().getSimpleName(), shipName, cells, hit);
    }
}
