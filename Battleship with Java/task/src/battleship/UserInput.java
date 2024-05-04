package battleship;

import java.util.Scanner;

public class UserInput {
    private final Scanner scan = new Scanner(System.in);
    public String prompt(String... message) {
        //System.out.println(message.toString());
        return scan.nextLine();
    }
}
