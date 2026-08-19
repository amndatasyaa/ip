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

        String[] tasks = new String[100];
        boolean[] isDone = new boolean[100];
        int taskCount = 0;
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine();

            if (command.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(divider);
                break;
            }

            if (command.equals("list")) {
                System.out.println(" Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    String status = isDone[i] ? "X" : " ";
                    System.out.println(" " + (i + 1) + ".[" + status + "] " + tasks[i]);
                }
            } else if (command.startsWith("mark ")) {
                int taskNumber = Integer.parseInt(command.substring(5));
                int taskIndex = taskNumber - 1;
                isDone[taskIndex] = true;
                System.out.println(" Nice! I've marked this task as done:");
                System.out.println("   [X] " + tasks[taskIndex]);
            } else {
                tasks[taskCount] = command;
                taskCount++;
                System.out.println(" added: " + command);
            }
            System.out.println(divider);
        }
    }
}
