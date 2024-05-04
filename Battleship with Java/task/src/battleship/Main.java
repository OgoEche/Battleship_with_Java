package battleship;

import java.util.List;

public class Main {

    public static void swapForBattle(Player one, Player two) {
        one.swapPlayItems(two.getPlayerRecord().myField(),
                two.getPlayerRecord().myFleet(), two.getPlayerRecord().parts2Ship());

        two.swapPlayItems(one.getPlayerRecord().myField(),
                one.getPlayerRecord().myFleet(), one.getPlayerRecord().parts2Ship());

    }

    public static void main(String[] args) {
        List<Player> playerList = List.of(new Player(1), new Player(2));

        for (var player: playerList) {
            player.loadFleet();
            player.afterEachMove();
        }

        swapForBattle(playerList.get(0), playerList.get(1));

        var status = false;
        while (!status) {

            for (var player: playerList) {
                player.displayPlayerField();
                status = player.play("FOG");
                if (status) break;
                player.afterEachMove();
            }
        }

    }
}
