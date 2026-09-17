package snoopy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        assert storage != null : "Storage must be provided";
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
        assert loadedTasks != null : "Storage must return a task list";
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
        assert commandType == CommandType.UNKNOWN
                || command.startsWith(commandType.getKeyword())
                : "Recognized commands must start with their keyword";

        try {
            return executeCommand(command, commandType);
        } catch (SnoopyException exception) {
            return " OOPS! " + exception.getMessage();
        } catch (IOException exception) {
            return " OOPS! I couldn't save the task list.";
        }
    }

    /**
     * Dispatches a recognized command to its command-specific handler.
     *
     * @param command Complete trimmed command.
     * @param commandType Type identified from the command.
     * @return Response to show the user.
     * @throws SnoopyException If the command arguments are invalid.
     * @throws IOException If a task-list change cannot be saved.
     */
    private String executeCommand(String command, CommandType commandType)
            throws SnoopyException, IOException {
        switch (commandType) {
            case BYE:
                shouldExit = true;
                return " Bye. Hope to see you again soon!";
            case LIST:
                return getTaskListResponse();
            case UNMARK:
                return unmarkTask(command, commandType);
            case MARK:
                return markTask(command, commandType);
            case DELETE:
                return deleteTask(command, commandType);
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
                        "Sorry, I don't recognize that command. Available commands: "
                                + CommandType.getCommandSummary() + ".");
            default:
                throw new IllegalStateException("Unexpected command type: " + commandType);
        }
    }

    /**
     * Marks the task selected by an unmark command as not done.
     *
     * @param command Complete unmark command.
     * @param commandType Unmark command metadata.
     * @return Confirmation shown to the user.
     * @throws SnoopyException If the task number is invalid.
     * @throws IOException If the task list cannot be saved.
     */
    private String unmarkTask(String command, CommandType commandType)
            throws SnoopyException, IOException {
        int taskIndex = getTaskIndex(command, commandType);
        Task task = tasks.get(taskIndex);
        updateTaskStatus(task, false);
        return formatLines(
                " OK, I've marked this task as not done yet:",
                "   " + task);
    }

    /**
     * Marks the task selected by a mark command as done.
     *
     * @param command Complete mark command.
     * @param commandType Mark command metadata.
     * @return Confirmation shown to the user.
     * @throws SnoopyException If the task number is invalid.
     * @throws IOException If the task list cannot be saved.
     */
    private String markTask(String command, CommandType commandType)
            throws SnoopyException, IOException {
        int taskIndex = getTaskIndex(command, commandType);
        Task task = tasks.get(taskIndex);
        updateTaskStatus(task, true);
        return formatLines(
                " Nice! I've marked this task as done:",
                "   " + task);
    }

    /**
     * Removes the task selected by a delete command.
     *
     * @param command Complete delete command.
     * @param commandType Delete command metadata.
     * @return Confirmation shown to the user.
     * @throws SnoopyException If the task number is invalid.
     * @throws IOException If the task list cannot be saved.
     */
    private String deleteTask(String command, CommandType commandType)
            throws SnoopyException, IOException {
        int taskIndex = getTaskIndex(command, commandType);
        Task removedTask = tasks.remove(taskIndex);
        try {
            storage.save(tasks);
        } catch (IOException exception) {
            tasks.add(taskIndex, removedTask);
            throw exception;
        }
        return formatLines(
                " Noted. I've removed this task:",
                "   " + removedTask,
                " Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Extracts and validates the task number from a task-selection command.
     *
     * @param command Complete task-selection command.
     * @param commandType Command metadata containing the keyword.
     * @return Zero-based index of the selected task.
     * @throws SnoopyException If the task number is invalid.
     */
    private int getTaskIndex(String command, CommandType commandType) throws SnoopyException {
        String numberText = getArguments(command, commandType);
        return parseTaskIndex(numberText, tasks.size(), commandType);
    }

    /**
     * Returns the trimmed arguments that follow a recognized command keyword.
     *
     * @param command Complete trimmed command.
     * @param commandType Type identified from the command.
     * @return Command arguments, or an empty string when none were supplied.
     */
    private static String getArguments(String command, CommandType commandType) {
        assert command.startsWith(commandType.getKeyword())
                : "Command must start with its recognized keyword";
        return command.substring(commandType.getKeyword().length()).trim();
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
        return " Here are the tasks in your list:" + formatNumberedTasks(tasks);
    }

    /**
     * Changes a task's completion state and restores it if saving fails.
     *
     * @param task Task whose state should change.
     * @param shouldMarkDone Whether the task should be marked as done.
     * @throws IOException If the task list cannot be saved.
     */
    private void updateTaskStatus(Task task, boolean shouldMarkDone) throws IOException {
        boolean wasDone = task.isDone();
        if (shouldMarkDone) {
            task.markAsDone();
        } else {
            task.markAsNotDone();
        }

        try {
            storage.save(tasks);
        } catch (IOException exception) {
            if (wasDone) {
                task.markAsDone();
            } else {
                task.markAsNotDone();
            }
            throw exception;
        }
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
        String keyword = getArguments(command, commandType);
        if (keyword.isEmpty()) {
            throw new SnoopyException("Please provide a keyword to find.");
        }

        List<Task> matchingTasks = tasks.stream()
                .filter(task -> task.containsKeyword(keyword))
                .toList();
        return " Here are the matching tasks in your list:" + formatNumberedTasks(matchingTasks);
    }

    /**
     * Formats tasks as a one-based numbered list, with each task on a new line.
     *
     * @param tasksToFormat Tasks to number in their existing order.
     * @return Numbered task lines, or an empty string when there are no tasks.
     */
    private static String formatNumberedTasks(List<Task> tasksToFormat) {
        assert tasksToFormat != null : "Task list must be provided for formatting";
        String numberedTasks = IntStream.range(0, tasksToFormat.size())
                .mapToObj(index -> " " + (index + 1) + "." + tasksToFormat.get(index))
                .collect(Collectors.joining("\n"));
        return numberedTasks.isEmpty() ? "" : "\n" + numberedTasks;
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
        String description = getArguments(command, commandType);
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
        if (hasRepeatedDelimiter(command, " /by ")) {
            throw new SnoopyException("A deadline must contain exactly one '/by' separator.");
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
        if (hasRepeatedDelimiter(command, " /from ") || hasRepeatedDelimiter(command, " /to ")) {
            throw new SnoopyException(
                    "An event must contain exactly one '/from' and one '/to' separator.");
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
        if (to.isBefore(from)) {
            throw new SnoopyException("The event end date cannot be before its start date.");
        }
        if (to.equals(from)) {
            throw new SnoopyException("The event end date must be after its start date.");
        }
        return saveNewTask(new Event(description, from, to));
    }

    /**
     * Checks whether a command contains a delimiter more than once.
     *
     * @param command Complete command to inspect.
     * @param delimiter Delimiter including its required surrounding spaces.
     * @return {@code true} when the delimiter occurs at least twice.
     */
    private static boolean hasRepeatedDelimiter(String command, String delimiter) {
        int firstIndex = command.indexOf(delimiter);
        return firstIndex >= 0 && command.indexOf(delimiter, firstIndex + delimiter.length()) >= 0;
    }

    /**
     * Persists a new task and creates its confirmation response.
     *
     * @param task Task to add and save.
     * @return Confirmation shown to the user.
     * @throws IOException If the task cannot be saved.
     */
    private String saveNewTask(Task task) throws IOException {
        assert task != null : "A new task must be created before it can be saved";
        tasks.add(task);
        assert tasks.get(tasks.size() - 1) == task : "The new task must be appended to the task list";
        try {
            storage.save(tasks);
        } catch (IOException exception) {
            tasks.remove(tasks.size() - 1);
            throw exception;
        }
        return formatLines(
                " Got it. I've added this task:",
                "   " + task,
                " Now you have " + tasks.size() + " tasks in the list.");
    }

    /**
     * Joins any number of response lines with newline characters.
     *
     * @param lines Response lines in display order.
     * @return Lines combined into one response.
     */
    private static String formatLines(String... lines) {
        assert lines != null : "Response lines must be provided";
        for (String line : lines) {
            assert line != null : "Response lines must not contain null values";
        }
        return String.join("\n", lines);
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
        assert commandType == CommandType.DEADLINE || commandType == CommandType.EVENT
                : "Only deadline and event commands contain dates";
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
        assert taskCount >= 0 : "Task count cannot be negative";
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
        int taskIndex = taskNumber - 1;
        assert taskIndex >= 0 && taskIndex < taskCount : "Parsed task index must be within the task list";
        return taskIndex;
    }
}
