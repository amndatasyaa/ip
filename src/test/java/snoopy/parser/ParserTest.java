package snoopy.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import snoopy.command.CommandType;
import snoopy.exception.SnoopyException;
import snoopy.task.Deadline;
import snoopy.task.Event;
import snoopy.task.Task;
import snoopy.task.Todo;

/**
 * Tests command recognition and parsing of task details and indexes.
 */
public class ParserTest {
    @Test
    public void parseCommandType_supportedAndInvalidCommands_returnsExpectedTypes() {
        assertEquals(CommandType.BYE, Parser.parseCommandType("bye"));
        assertEquals(CommandType.LIST, Parser.parseCommandType("list"));
        assertEquals(CommandType.MARK, Parser.parseCommandType("mark 2"));
        assertEquals(CommandType.TODO, Parser.parseCommandType("todo   read book"));
        assertEquals(CommandType.UNKNOWN, Parser.parseCommandType(""));
        assertEquals(CommandType.UNKNOWN, Parser.parseCommandType("TODO read book"));
        assertEquals(CommandType.UNKNOWN, Parser.parseCommandType("list all"));
        assertEquals(CommandType.UNKNOWN, Parser.parseCommandType("bye now"));
    }

    @Test
    public void parseTask_validAddCommands_returnsTypedTasksWithParsedDates()
            throws SnoopyException {
        Task todo = Parser.parseTask("todo read book", CommandType.TODO);
        Task deadline = Parser.parseTask("deadline return book /by 2026-08-30",
                CommandType.DEADLINE);
        Task event = Parser.parseTask(
                "event meeting /from 2026-09-01 /to 2026-09-02", CommandType.EVENT);

        assertInstanceOf(Todo.class, todo);
        assertInstanceOf(Deadline.class, deadline);
        assertInstanceOf(Event.class, event);
        assertEquals("T | 0 | read book", todo.toDataString());
        assertEquals("D | 0 | return book | 2026-08-30", deadline.toDataString());
        assertEquals("E | 0 | meeting | 2026-09-01 | 2026-09-02", event.toDataString());
    }

    @Test
    public void parseTask_missingDetails_reportsSpecificErrors() {
        assertParsingError("Please tell me what to add after 'todo'.",
                "todo", CommandType.TODO);
        assertParsingError("Please use: deadline <description> /by <date or time>.",
                "deadline return book", CommandType.DEADLINE);
        assertParsingError("A deadline needs both a description and a '/by' value.",
                "deadline /by 2026-08-30", CommandType.DEADLINE);
        assertParsingError("Please use: event <description> /from <start> /to <end>.",
                "event meeting /from 2026-09-01", CommandType.EVENT);
        assertParsingError(
                "An event needs a description, a '/from' value, and a '/to' value.",
                "event meeting /from /to 2026-09-02", CommandType.EVENT);
    }

    @Test
    public void parseTask_invalidDate_reportsCommandSpecificError() {
        assertParsingError(
                "Please enter the deadline date as yyyy-MM-dd, for example 2019-10-15.",
                "deadline return book /by Friday", CommandType.DEADLINE);
        assertParsingError(
                "Please enter event dates as yyyy-MM-dd, for example 2019-10-15.",
                "event meeting /from 2026-09-01 /to Tuesday", CommandType.EVENT);
    }

    @Test
    public void parseTaskIndex_validNumber_returnsZeroBasedIndex() throws SnoopyException {
        assertEquals(1, Parser.parseTaskIndex("mark 2", 3, CommandType.MARK));
    }

    @Test
    public void parseTaskIndex_invalidNumbers_reportsSpecificErrors() {
        assertIndexError("Please provide a task number, for example 'mark 2'.",
                "mark", 2);
        assertIndexError("'abc' is not a valid task number.", "mark abc", 2);
        assertIndexError("Your task list is empty, so there is no task to mark.",
                "mark 1", 0);
        assertIndexError("Task 3 does not exist. Choose a number from 1 to 2.",
                "mark 3", 2);
    }

    private void assertParsingError(String expectedMessage, String command,
            CommandType commandType) {
        SnoopyException exception = assertThrows(SnoopyException.class,
                () -> Parser.parseTask(command, commandType));
        assertEquals(expectedMessage, exception.getMessage());
    }

    private void assertIndexError(String expectedMessage, String command, int taskCount) {
        SnoopyException exception = assertThrows(SnoopyException.class,
                () -> Parser.parseTaskIndex(command, taskCount, CommandType.MARK));
        assertEquals(expectedMessage, exception.getMessage());
    }
}
