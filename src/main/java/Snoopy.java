import java.util.Scanner;

/**
 * Starts the Snoopy chatbot application.
 */
public class Snoopy {
    /**
     * Runs the chatbot until the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        String divider = "____________________________________________________________";
        String banner = "  ____\n"
                + " / ___| _ __   ___   ___  _ __  _   _\n"
                + " \\___ \\| '_ \\ / _ \\ / _ \\| '_ \\| | | |\n"
                + "  ___) | | | | (_) | (_) | |_) | |_| |\n"
                + " |____/|_| |_|\\___/ \\___/| .__/ \\__, |\n"
                + "                            |_|    |___/";

        System.out.println(divider);
        System.out.println(banner);
        System.out.println("Hi! I'm Snoopy, your happy little helper.");
        System.out.println("What can I do for you?");
        System.out.println(divider);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(divider);
                break;
            }

            System.out.println(" " + command);
            System.out.println(divider);
        }
    }
}
