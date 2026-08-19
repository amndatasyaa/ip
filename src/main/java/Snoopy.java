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

        Task[] tasks = new Task[100];
        int taskCount = 0;
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();

            if (command.equals("bye")) {
                System.out.println(" Bye. Hope to see you again soon!");
                System.out.println(divider);
                break;
            }

            try {
                if (command.equals("list")) {
                    System.out.println(" Here are the tasks in your list:");
                    for (int i = 0; i < taskCount; i++) {
                        System.out.println(" " + (i + 1) + "." + tasks[i]);
                    }
                } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                    String numberText = command.substring(6).trim();
                    int taskIndex = parseTaskIndex(numberText, taskCount);
                    tasks[taskIndex].markAsNotDone();
                    System.out.println(" OK, I've marked this task as not done yet:");
                    System.out.println("   " + tasks[taskIndex]);
                } else if (command.equals("mark") || command.startsWith("mark ")) {
                    String numberText = command.substring(4).trim();
                    int taskIndex = parseTaskIndex(numberText, taskCount);
                    tasks[taskIndex].markAsDone();
                    System.out.println(" Nice! I've marked this task as done:");
                    System.out.println("   " + tasks[taskIndex]);
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring(4).trim();
                    if (description.isEmpty()) {
                        throw new SnoopyException("Please tell me what to add after 'todo'.");
                    }
                    if (taskCount >= tasks.length) {
                        throw new SnoopyException("Your task list is full, so I can't add another task.");
                    }
                    tasks[taskCount] = new Todo(description);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + tasks[taskCount]);
                    taskCount++;
                    System.out.println(" Now you have " + taskCount + " tasks in the list.");
                } else if (command.equals("deadline") || command.startsWith("deadline ")) {
                    int byIndex = command.indexOf(" /by ");
                    if (byIndex < 0) {
                        throw new SnoopyException(
                                "Please use: deadline <description> /by <date or time>.");
                    }
                    String description = command.substring(8, byIndex).trim();
                    String by = command.substring(byIndex + 5).trim();
                    if (description.isEmpty() || by.isEmpty()) {
                        throw new SnoopyException(
                                "A deadline needs both a description and a '/by' value.");
                    }
                    if (taskCount >= tasks.length) {
                        throw new SnoopyException("Your task list is full, so I can't add another task.");
                    }
                    tasks[taskCount] = new Deadline(description, by);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + tasks[taskCount]);
                    taskCount++;
                    System.out.println(" Now you have " + taskCount + " tasks in the list.");
                } else if (command.equals("event") || command.startsWith("event ")) {
                    int fromIndex = command.indexOf(" /from ");
                    int toIndex = command.indexOf(" /to ");
                    if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
                        throw new SnoopyException(
                                "Please use: event <description> /from <start> /to <end>.");
                    }
                    if (fromIndex + 7 > toIndex) {
                        throw new SnoopyException(
                                "An event needs a description, a '/from' value, and a '/to' value.");
                    }
                    String description = command.substring(5, fromIndex).trim();
                    String from = command.substring(fromIndex + 7, toIndex).trim();
                    String to = command.substring(toIndex + 5).trim();
                    if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                        throw new SnoopyException(
                                "An event needs a description, a '/from' value, and a '/to' value.");
                    }
                    if (taskCount >= tasks.length) {
                        throw new SnoopyException("Your task list is full, so I can't add another task.");
                    }
                    tasks[taskCount] = new Event(description, from, to);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + tasks[taskCount]);
                    taskCount++;
                    System.out.println(" Now you have " + taskCount + " tasks in the list.");
                } else {
                    throw new SnoopyException(
                            "Sorry, I don't recognize that command. Try todo, deadline, event, list, mark, or unmark.");
                }
            } catch (SnoopyException exception) {
                System.out.println(" OOPS! " + exception.getMessage());
            }
            System.out.println(divider);
        }
    }

    /**
     * Converts a user-supplied task number into a valid array index.
     *
     * @param numberText task number entered by the user
     * @param taskCount number of tasks currently stored
     * @return the zero-based index of the selected task
     * @throws SnoopyException if the number is missing, non-numeric, or outside the list
     */
    private static int parseTaskIndex(String numberText, int taskCount) throws SnoopyException {
        if (numberText.isEmpty()) {
            throw new SnoopyException("Please provide a task number, for example 'mark 2'.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(numberText);
        } catch (NumberFormatException exception) {
            throw new SnoopyException("'" + numberText + "' is not a valid task number.");
        }

        if (taskCount == 0) {
            throw new SnoopyException("Your task list is empty, so there is nothing to mark or unmark.");
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new SnoopyException(
                    "Task " + taskNumber + " does not exist. Choose a number from 1 to " + taskCount + ".");
        }
        return taskNumber - 1;
    }
}
