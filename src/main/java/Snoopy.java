import java.io.IOException;
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

        Storage storage = new Storage();
        ArrayList<Task> tasks;
        try {
            tasks = storage.load();
        } catch (SnoopyException exception) {
            tasks = new ArrayList<>();
            System.out.println(" OOPS! " + exception.getMessage());
            System.out.println(divider);
        } catch (IOException exception) {
            tasks = new ArrayList<>();
            System.out.println(" OOPS! I couldn't load the saved tasks.");
            System.out.println(divider);
        }
        Scanner scanner = new Scanner(System.in);
        chatLoop:
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            CommandType commandType = CommandType.fromCommand(command);

            try {
                switch (commandType) {
                case BYE:
                    System.out.println(" Bye. Hope to see you again soon!");
                    System.out.println(divider);
                    break chatLoop;
                case LIST:
                    System.out.println(" Here are the tasks in your list:");
                    for (int i = 0; i < tasks.size(); i++) {
                        System.out.println(" " + (i + 1) + "." + tasks.get(i));
                    }
                    break;
                case UNMARK:
                    String numberText = command.substring(commandType.getKeyword().length()).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsNotDone();
                    storage.save(tasks);
                    System.out.println(" OK, I've marked this task as not done yet:");
                    System.out.println("   " + tasks.get(taskIndex));
                    break;
                case MARK:
                    numberText = command.substring(commandType.getKeyword().length()).trim();
                    taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsDone();
                    storage.save(tasks);
                    System.out.println(" Nice! I've marked this task as done:");
                    System.out.println("   " + tasks.get(taskIndex));
                    break;
                case DELETE:
                    numberText = command.substring(commandType.getKeyword().length()).trim();
                    taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    Task removedTask = tasks.remove(taskIndex);
                    storage.save(tasks);
                    System.out.println(" Noted. I've removed this task:");
                    System.out.println("   " + removedTask);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
                    break;
                case TODO:
                    String description = command.substring(commandType.getKeyword().length()).trim();
                    if (description.isEmpty()) {
                        throw new SnoopyException("Please tell me what to add after 'todo'.");
                    }
                    Task task = new Todo(description);
                    tasks.add(task);
                    storage.save(tasks);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + task);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
                    break;
                case DEADLINE:
                    int byIndex = command.indexOf(" /by ");
                    if (byIndex < 0) {
                        throw new SnoopyException(
                                "Please use: deadline <description> /by <date or time>.");
                    }
                    description = command.substring(commandType.getKeyword().length(), byIndex).trim();
                    String by = command.substring(byIndex + 5).trim();
                    if (description.isEmpty() || by.isEmpty()) {
                        throw new SnoopyException(
                                "A deadline needs both a description and a '/by' value.");
                    }
                    task = new Deadline(description, by);
                    tasks.add(task);
                    storage.save(tasks);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + task);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
                    break;
                case EVENT:
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
                    description = command.substring(commandType.getKeyword().length(), fromIndex).trim();
                    String from = command.substring(fromIndex + 7, toIndex).trim();
                    String to = command.substring(toIndex + 5).trim();
                    if (description.isEmpty() || from.isEmpty() || to.isEmpty()) {
                        throw new SnoopyException(
                                "An event needs a description, a '/from' value, and a '/to' value.");
                    }
                    task = new Event(description, from, to);
                    tasks.add(task);
                    storage.save(tasks);
                    System.out.println(" Got it. I've added this task:");
                    System.out.println("   " + task);
                    System.out.println(" Now you have " + tasks.size() + " tasks in the list.");
                    break;
                case UNKNOWN:
                    throw new SnoopyException(
                            "Sorry, I don't recognize that command. "
                                    + "Try todo, deadline, event, list, mark, unmark, or delete.");
                }
            } catch (SnoopyException exception) {
                System.out.println(" OOPS! " + exception.getMessage());
            } catch (IOException exception) {
                System.out.println(" OOPS! I couldn't save the task list.");
            }
            System.out.println(divider);
        }
    }

    /**
     * Converts a user-supplied task number into a valid array index.
     *
     * @param numberText task number entered by the user
     * @param taskCount number of tasks currently stored
     * @param commandType command that requires the task number
     * @return the zero-based index of the selected task
     * @throws SnoopyException if the number is missing, non-numeric, or outside the list
     */
    private static int parseTaskIndex(String numberText, int taskCount, CommandType commandType)
            throws SnoopyException {
        if (numberText.isEmpty()) {
            throw new SnoopyException(
                    "Please provide a task number, for example '"
                            + commandType.getKeyword() + " 2'.");
        }

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(numberText);
        } catch (NumberFormatException exception) {
            throw new SnoopyException("'" + numberText + "' is not a valid task number.");
        }

        if (taskCount == 0) {
            throw new SnoopyException(
                    "Your task list is empty, so there is no task to "
                            + commandType.getKeyword() + ".");
        }
        if (taskNumber < 1 || taskNumber > taskCount) {
            throw new SnoopyException(
                    "Task " + taskNumber + " does not exist. Choose a number from 1 to " + taskCount + ".");
        }
        return taskNumber - 1;
    }
}
