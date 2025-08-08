package ui;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final Client client;

    public Repl(String serverUrl) {
        client = new Client(serverUrl, this);
    }

    public void run() {
        System.out.print(SET_BG_COLOR_WHITE);
        System.out.println("Welcome to 240 Chess. Type Help to get started.");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";

        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                if (!result.isEmpty()) {
                    System.out.print(SET_TEXT_COLOR_BLUE + result);
                }
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }

        System.out.println();
    }

    public void printPrompt() {
        String stateDisplay = Client.state == State.IN_GAME ? "IN_GAME" : Client.state.toString();
        System.out.print(SET_TEXT_COLOR_BLACK + "\n" + "[" + stateDisplay + "] >>> " + SET_TEXT_COLOR_GREEN);
    }

    public void showNotification(String message) {
        System.out.println(SET_TEXT_COLOR_MAGENTA + "\n📢 Notification: " + message);
    }
}
