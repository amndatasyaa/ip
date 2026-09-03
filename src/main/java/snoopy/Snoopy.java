package snoopy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Scanner;

import snoopy.command.CommandType;
import snoopy.exception.SnoopyException;
import snoopy.storage.Storage;
import snoopy.task.Deadline;
import snoopy.task.Event;
import snoopy.task.Task;
import snoopy.task.Todo;

/**
 * Processes Snoopy commands for both the text and graphical user interfaces.
 */
public class Snoopy {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "  ____\n"
            + " / ___| _ __   ___   ___  _ __  _   _\n"
            + " \\___ \\| '_ \\ / _ \\ / _ \\| '_ \\| | | |\n"
            + "  ___) | | | | (_) | (_) | |_) | |_| |\n"
            + " |____/|_| |_|\\___/ \\___/| .__/ \\__, |\n"
            + "                            |_|    |___/";
    private static final String WELCOME_MESSAGE = "Hi! I'm Snoopy, your happy little helper.\n"
            + "What can I do for you?";

    private final Storage storage;
    private final ArrayList<Task> tasks;
    private final String startupError;
    private boolean shouldExit;

    /**
     * Creates Snoopy using the standard data-file location.
     */
    public Snoopy() {
        this(new Storage());
    }

    /**
     * Creates Snoopy with the supplied storage implementation.
     *
     * @param storage Storage used to load and save tasks.
     */
    public Snoopy(Storage storage) {
        this.storage = storage;

        ArrayList<Task> loadedTasks;
        String loadError = null;
        try {
            loadedTasks = storage.load();
        } catch (SnoopyException exception) {
            loadedTasks = new ArrayList<>();
            loadError = " OOPS! " + exception.getMessage();
        } catch (IOException exception) {
            loadedTasks = new ArrayList<>();
            loadError = " OOPS! I couldn't load the saved tasks.";
        }
        tasks = loadedTasks;
        startupError = loadError;
    }

    /**
     * Runs the text interface until the user enters {@code bye}.
     *
     * @param args Command-line arguments; not used by this application.
     */
    public static void main(String[] args) {
        Snoopy snoopy = new Snoopy();

        System.out.println(DIVIDER);
        System.out.println(BANNER);
        System.out.println(WELCOME_MESSAGE);
        System.out.println(DIVIDER);
        if (snoopy.startupError != null) {
            System.out.println(snoopy.startupError);
            System.out.println(DIVIDER);
        }

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine() && !snoopy.shouldExit()) {
            System.out.println(snoopy.getResponse(scanner.nextLine()));
            System.out.println(DIVIDER);
        }
    }

    /**
     * Returns the greeting shown when the graphical interface starts.
     *
     * @return Greeting and any storage-loading error.
     */
    public String getWelcomeMessage() {
        if (startupError == null) {
            return WELCOME_MESSAGE;
        }
        return WELCOME_MESSAGE + "\n\n" + startupError.stripLeading();
    }

    /**
     * Processes one command and returns the response to show the user.
     *
     * @param input Command entered by the user.
     * @return Snoopy's response, which can contain multiple lines.
     */
    public String getResponse(String input) {
        String command = input == null ? "" : input.trim();
        CommandType commandType = CommandType.fromCommand(command);

        try {
            switch (commandType) {
                case BYE:
                    shouldExit = true;
                    return " Bye. Hope to see you again soon!";
                case LIST:
                    return getTaskListResponse();
                case UNMARK: {
                    String numberText = command.substring(commandType.getKeyword().length()).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsNotDone();
                    storage.save(tasks);
                    return " OK, I've marked this task as not done yet:\n"
                            + "   " + tasks.get(taskIndex);
                }
                case MARK: {
                    String numberText = command.substring(commandType.getKeyword().length()).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsDone();
                    storage.save(tasks);
                    return " Nice! I've marked this task as done:\n"
                            + "   " + tasks.get(taskIndex);
                }
                case DELETE: {
                    String numberText = command.substring(commandType.getKeyword().length()).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    Task removedTask = tasks.remove(taskIndex);
                    storage.save(tasks);
                    return " Noted. I've removed this task:\n"
                            + "   " + removedTask + "\n"
                            + " Now you have " + tasks.size() + " tasks in the list.";
                }
                case FIND:
                    return getFindResponse(command, commandType);
                case TODO:
                    return addTodo(command, commandType);
                case DEADLINE:
                    return addDeadline(command, commandType);
                case EVENT:
                    return addEvent(command, commandType);
                case UNKNOWN:
                    throw new SnoopyException(
                            "Sorry, I don't recognize that command. "
                                    + "Try todo, deadline, event, list, mark, unmark, delete, or find.");
                default:
                    throw new IllegalStateException("Unexpected command type: " + commandType);
            }
        } catch (SnoopyException exception) {
            return " OOPS! " + exception.getMessage();
        } catch (IOException exception) {
            return " OOPS! I couldn't save the task list.";
        }
    }

    /**
     * Checks whether the last command requested that the application close.
     *
     * @return {@code true} after a valid {@code bye} command.
     */
    public boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Formats every task for the list command.
     *
     * @return Response containing the task list.
     */
    private String getTaskListResponse() {
        StringBuilder response = new StringBuilder(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            response.append("\n ").append(i + 1).append(".").append(tasks.get(i));
        }
        return response.toString();
    }

    /**
     * Finds tasks whose descriptions contain the requested keyword.
     *
     * @param command Complete find command.
     * @param commandType Find command metadata.
     * @return Response containing matching tasks.
     * @throws SnoopyException If the keyword is empty.
     */
    private String getFindResponse(String command, CommandType commandType) throws SnoopyException {
        String keyword = command.substring(commandType.getKeyword().length()).trim();
        if (keyword.isEmpty()) {
            throw new SnoopyException("Please provide a keyword to find.");
        }

        StringBuilder response = new StringBuilder(" Here are the matching tasks in your list:");
        int matchNumber = 1;
        for (Task currentTask : tasks) {
            if (currentTask.containsKeyword(keyword)) {
                response.append("\n ").append(matchNumber).append(".").append(currentTask);
                matchNumber++;
            }
        }
        return response.toString();
    }

    /**
     * Adds a todo from a validated command.
     *
     * @param command Complete todo command.
     * @param commandType Todo command metadata.
     * @return Confirmation shown to the user.
     * @throws SnoopyException If the description is empty.
     * @throws IOException If the task cannot be saved.
     */
    private String addTodo(String command, CommandType commandType) throws SnoopyException, IOException {
        String description = command.substring(commandType.getKeyword().length()).trim();
        if (description.isEmpty()) {
            throw new SnoopyException("Please tell me what to add after 'todo'.");
        }
        return saveNewTask(new Todo(description));
    }

    /**
     * Adds a deadline from a validated command.
     *
     * @param command Complete deadline command.
     * @param commandType Deadline command metadata.
     * @return Confirmation shown to the user.
     * @throws SnoopyException If required fields or the date are invalid.
     * @throws IOException If the task cannot be saved.
     */
    private String addDeadline(String command, CommandType commandType)
            throws SnoopyException, IOException {
        int byIndex = command.indexOf(" /by ");
        if (byIndex < 0) {
            throw new SnoopyException("Please use: deadline <description> /by <date or time>.");
        }
        String description = command.substring(commandType.getKeyword().length(), byIndex).trim();
        String byText = command.substring(byIndex + 5).trim();
        if (description.isEmpty() || byText.isEmpty()) {
            throw new SnoopyException("A deadline needs both a description and a '/by' value.");
        }

        LocalDate by = parseDate(byText, commandType);
        return saveNewTask(new Deadline(description, by));
    }

    /**
     * Adds an event from a validated command.
     *
     * @param command Complete event command.
     * @param commandType Event command metadata.
     * @return Confirmation shown to the user.
     * @throws SnoopyException If required fields or either date are invalid.
     * @throws IOException If the task cannot be saved.
     */
    private String addEvent(String command, CommandType commandType) throws SnoopyException, IOException {
        int fromIndex = command.indexOf(" /from ");
        int toIndex = command.indexOf(" /to ");
        if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
            throw new SnoopyException("Please use: event <description> /from <start> /to <end>.");
        }
        if (fromIndex + 7 > toIndex) {
            throw new SnoopyException(
                    "An event needs a description, a '/from' value, and a '/to' value.");
        }

        String description = command.substring(commandType.getKeyword().length(), fromIndex).trim();
        String fromText = command.substring(fromIndex + 7, toIndex).trim();
        String toText = command.substring(toIndex + 5).trim();
        if (description.isEmpty() || fromText.isEmpty() || toText.isEmpty()) {
            throw new SnoopyException(
                    "An event needs a description, a '/from' value, and a '/to' value.");
        }

        LocalDate from = parseDate(fromText, commandType);
        LocalDate to = parseDate(toText, commandType);
        return saveNewTask(new Event(description, from, to));
    }

    /**
     * Persists a new task and creates its confirmation response.
     *
     * @param task Task to add and save.
     * @return Confirmation shown to the user.
     * @throws IOException If the task cannot be saved.
     */
    private String saveNewTask(Task task) throws IOException {
        tasks.add(task);
        storage.save(tasks);
        return " Got it. I've added this task:\n"
                + "   " + task + "\n"
                + " Now you have " + tasks.size() + " tasks in the list.";
    }

    /**
     * Parses an ISO date used by a deadline or event command.
     *
     * @param dateText Date entered by the user.
     * @param commandType Command whose date is being parsed.
     * @return Parsed date.
     * @throws SnoopyException If the text is not a valid {@code yyyy-MM-dd} date.
     */
    private static LocalDate parseDate(String dateText, CommandType commandType)
            throws SnoopyException {
        try {
            return LocalDate.parse(dateText);
        } catch (DateTimeParseException exception) {
            String subject = commandType == CommandType.DEADLINE
                    ? "the deadline date" : "event dates";
            throw new SnoopyException(
                    "Please enter " + subject
                            + " as yyyy-MM-dd, for example 2019-10-15.");
        }
    }

    /**
     * Converts a user-supplied task number into a valid array index.
     *
     * @param numberText Task number entered by the user.
     * @param taskCount Number of tasks currently stored.
     * @param commandType Command that requires the task number.
     * @return The zero-based index of the selected task.
     * @throws SnoopyException If the number is missing, non-numeric, or outside the list.
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
