package snoopy;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import snoopy.command.CommandType;
import snoopy.exception.SnoopyException;
import snoopy.storage.Storage;
import snoopy.task.Deadline;
import snoopy.task.Event;
import snoopy.task.Task;
import snoopy.task.TaskList;
import snoopy.task.Todo;
import snoopy.ui.Ui;

/**
 * Starts the Snoopy chatbot application.
 */
public class Snoopy {
    private final Storage storage;
    private final Ui ui;
    private TaskList tasks;

    /**
     * Creates a chatbot using the standard console and data file.
     */
    public Snoopy() {
        this.storage = new Storage();
        this.ui = new Ui();
    }

    /**
     * Runs the chatbot until the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        new Snoopy().run();
    }

    /**
     * Runs the chatbot until the user enters {@code bye} or input ends.
     */
    public void run() {
        ui.showWelcome();
        try {
            tasks = new TaskList(storage.load());
        } catch (SnoopyException exception) {
            tasks = new TaskList();
            ui.showError(exception.getMessage());
            ui.showLine();
        } catch (IOException exception) {
            tasks = new TaskList();
            ui.showLoadingError();
            ui.showLine();
        }

        chatLoop:
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            CommandType commandType = CommandType.fromCommand(command);

            try {
                switch (commandType) {
                case BYE:
                    ui.showGoodbye();
                    ui.showLine();
                    break chatLoop;
                case LIST:
                    ui.showTaskList(tasks);
                    break;
                case UNMARK:
                    String numberText = command.substring(commandType.getKeyword().length()).trim();
                    int taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsNotDone();
                    storage.save(tasks);
                    ui.showTaskUnmarked(tasks.get(taskIndex));
                    break;
                case MARK:
                    numberText = command.substring(commandType.getKeyword().length()).trim();
                    taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsDone();
                    storage.save(tasks);
                    ui.showTaskMarked(tasks.get(taskIndex));
                    break;
                case DELETE:
                    numberText = command.substring(commandType.getKeyword().length()).trim();
                    taskIndex = parseTaskIndex(numberText, tasks.size(), commandType);
                    Task removedTask = tasks.remove(taskIndex);
                    storage.save(tasks);
                    ui.showTaskDeleted(removedTask, tasks.size());
                    break;
                case TODO:
                    String description = command.substring(commandType.getKeyword().length()).trim();
                    if (description.isEmpty()) {
                        throw new SnoopyException("Please tell me what to add after 'todo'.");
                    }
                    Task task = new Todo(description);
                    tasks.add(task);
                    storage.save(tasks);
                    ui.showTaskAdded(task, tasks.size());
                    break;
                case DEADLINE:
                    int byIndex = command.indexOf(" /by ");
                    if (byIndex < 0) {
                        throw new SnoopyException(
                                "Please use: deadline <description> /by <date or time>.");
                    }
                    description = command.substring(commandType.getKeyword().length(), byIndex).trim();
                    String byText = command.substring(byIndex + 5).trim();
                    if (description.isEmpty() || byText.isEmpty()) {
                        throw new SnoopyException(
                                "A deadline needs both a description and a '/by' value.");
                    }
                    LocalDate by = parseDate(byText, commandType);
                    task = new Deadline(description, by);
                    tasks.add(task);
                    storage.save(tasks);
                    ui.showTaskAdded(task, tasks.size());
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
                    String fromText = command.substring(fromIndex + 7, toIndex).trim();
                    String toText = command.substring(toIndex + 5).trim();
                    if (description.isEmpty() || fromText.isEmpty() || toText.isEmpty()) {
                        throw new SnoopyException(
                                "An event needs a description, a '/from' value, and a '/to' value.");
                    }
                    LocalDate from = parseDate(fromText, commandType);
                    LocalDate to = parseDate(toText, commandType);
                    task = new Event(description, from, to);
                    tasks.add(task);
                    storage.save(tasks);
                    ui.showTaskAdded(task, tasks.size());
                    break;
                case UNKNOWN:
                    throw new SnoopyException(
                            "Sorry, I don't recognize that command. "
                                    + "Try todo, deadline, event, list, mark, unmark, or delete.");
                }
            } catch (SnoopyException exception) {
                ui.showError(exception.getMessage());
            } catch (IOException exception) {
                ui.showSavingError();
            }
            ui.showLine();
        }
    }

    /**
     * Parses an ISO date used by a deadline or event command.
     *
     * @param dateText date entered by the user
     * @param commandType command whose date is being parsed
     * @return parsed date
     * @throws SnoopyException if the text is not a valid {@code yyyy-MM-dd} date
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
