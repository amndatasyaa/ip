import java.util.ArrayList;
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

        ArrayList<Task> tasks = new ArrayList<>();
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
                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println(" " + (i + 1) + "." + tasks.get(i));
                    }
                } else if (command.equals("unmark") || command.startsWith("unmark ")) {
                    String numberText = command.substring(6).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), "unmark");
                    tasks.get(taskIndex).markAsNotDone();
                    System.out.println(" OK, I've marked this task as not done yet:");
                    System.out.println("   " + tasks.get(taskIndex));
                } else if (command.equals("mark") || command.startsWith("mark ")) {
                    String numberText = command.substring(4).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), "mark");
                    tasks.get(taskIndex).markAsDone();
                    System.out.println(" Nice! I've marked this task as done:");
                    System.out.println("   " + tasks.get(taskIndex));
                } else if (command.equals("delete") || command.startsWith("delete ")) {
                    String numberText = command.substring(6).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), "delete");
                    Task removedTask = tasks.remove(taskIndex);
                    System.out.println(" Noted. I've removed this task:");
                    System.out.println("   " + removedTask);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
                } else if (command.equals("todo") || command.startsWith("todo ")) {
                    String description = command.substring(4).trim();
                    if (description.isEmpty()) {
                        throw new SnoopyException("Please tell me what to add after 'todo'.");
                    }
                    Task task = new Todo(description);
                    tasks.add(task);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + task);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
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
                    Task task = new Deadline(description, by);
                    tasks.add(task);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + task);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
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
                    Task task = new Event(description, from, to);
                    tasks.add(task);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + task);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
                } else {
                    throw new SnoopyException(
                            "Sorry, I don't recognize that command. "
                                    + "Try todo, deadline, event, list, mark, unmark, or delete.");
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
     * @param commandName command that requires the task number
     * @return the zero-based index of the selected task
     * @throws SnoopyException if the number is missing, non-numeric, or outside the list
     */
    private static int parseTaskIndex(String numberText, int taskCount, String commandName)
            throws SnoopyException {
        if (numberText.isEmpty()) {
            throw new SnoopyException(
                    "Please provide a task number, for example '" + commandName + " 2'.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(numberText);
        } catch (NumberFormatException exception) {
            throw new SnoopyException("'" + numberText + "' is not a valid task number.");
        }

        if (taskCount == 0) {
            throw new SnoopyException(
                    "Your task list is empty, so there is no task to " + commandName + ".");
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new SnoopyException(
                    "Task " + taskNumber + " does not exist. Choose a number from 1 to " + taskCount + ".");
        }
        return taskNumber - 1;
    }
}
