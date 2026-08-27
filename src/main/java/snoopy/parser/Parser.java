package snoopy.parser;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import snoopy.command.CommandType;
import snoopy.exception.SnoopyException;
import snoopy.task.Deadline;
import snoopy.task.Event;
import snoopy.task.Task;
import snoopy.task.Todo;

/**
 * Interprets user commands and converts their arguments into application data.
 */
public final class Parser {
    private Parser() {
        // This utility class stores no state and should not be instantiated.
    }

    /**
     * Identifies a command from the first word of the user's input.
     *
     * @param command complete trimmed input from the user
     * @return matching command type, or {@link CommandType#UNKNOWN} if none matches
     */
    public static CommandType parseCommandType(String command) {
        if (command.isEmpty()) {
            return CommandType.UNKNOWN;
        }

        String firstWord = command.split("\\s+", 2)[0];
        for (CommandType type : CommandType.values()) {
            if (type.getKeyword().equals(firstWord)) {
                boolean acceptsArguments = type != CommandType.BYE && type != CommandType.LIST;
                if (!acceptsArguments && !command.equals(type.getKeyword())) {
                    return CommandType.UNKNOWN;
                }
                return type;
            }
        }
        return CommandType.UNKNOWN;
    }

    /**
     * Parses an add command into the corresponding task object.
     *
     * @param command complete command entered by the user
     * @param commandType type of add command being parsed
     * @return task described by the command
     * @throws SnoopyException if required task details are missing or invalid
     */
    public static Task parseTask(String command, CommandType commandType)
            throws SnoopyException {
        switch (commandType) {
        case TODO:
            return parseTodo(command);
        case DEADLINE:
            return parseDeadline(command);
        case EVENT:
            return parseEvent(command);
        default:
            throw new IllegalArgumentException("Cannot parse a task from " + commandType);
        }
    }

    /**
     * Converts a user-supplied task number into a valid zero-based index.
     *
     * @param command complete command entered by the user
     * @param taskCount number of tasks currently stored
     * @param commandType command that requires the task number
     * @return zero-based index of the selected task
     * @throws SnoopyException if the number is missing, non-numeric, or outside the list
     */
    public static int parseTaskIndex(String command, int taskCount, CommandType commandType)
            throws SnoopyException {
        String numberText = command.substring(commandType.getKeyword().length()).trim();
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
                    "Task " + taskNumber + " does not exist. Choose a number from 1 to "
                            + taskCount + ".");
        }
        return taskNumber - 1;
    }

    /**
     * Extracts the required keyword from a find command.
     *
     * @param command complete find command entered by the user
     * @return non-empty keyword to search for
     * @throws SnoopyException if the command has no keyword
     */
    public static String parseFindKeyword(String command) throws SnoopyException {
        String keyword = command.substring(CommandType.FIND.getKeyword().length()).trim();
        if (keyword.isEmpty()) {
            throw new SnoopyException("Please provide a keyword to find.");
        }
        return keyword;
    }

    private static Todo parseTodo(String command) throws SnoopyException {
        String description = command.substring(CommandType.TODO.getKeyword().length()).trim();
        if (description.isEmpty()) {
            throw new SnoopyException("Please tell me what to add after 'todo'.");
        }
        return new Todo(description);
    }

    private static Deadline parseDeadline(String command) throws SnoopyException {
        int byIndex = command.indexOf(" /by ");
        if (byIndex < 0) {
            throw new SnoopyException(
                    "Please use: deadline <description> /by <date or time>.");
        }

        String description = command.substring(CommandType.DEADLINE.getKeyword().length(),
                byIndex).trim();
        String byText = command.substring(byIndex + 5).trim();
        if (description.isEmpty() || byText.isEmpty()) {
            throw new SnoopyException(
                    "A deadline needs both a description and a '/by' value.");
        }
        return new Deadline(description, parseDate(byText, CommandType.DEADLINE));
    }

    private static Event parseEvent(String command) throws SnoopyException {
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

        String description = command.substring(CommandType.EVENT.getKeyword().length(),
                fromIndex).trim();
        String fromText = command.substring(fromIndex + 7, toIndex).trim();
        String toText = command.substring(toIndex + 5).trim();
        if (description.isEmpty() || fromText.isEmpty() || toText.isEmpty()) {
            throw new SnoopyException(
                    "An event needs a description, a '/from' value, and a '/to' value.");
        }
        return new Event(description, parseDate(fromText, CommandType.EVENT),
                parseDate(toText, CommandType.EVENT));
    }

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
}
