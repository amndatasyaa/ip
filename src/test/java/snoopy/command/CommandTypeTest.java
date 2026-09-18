package snoopy.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests command recognition and command-word boundaries.
 */
public class CommandTypeTest {
    @Test
    public void getCommandSummary_allSupportedCommands_excludesUnknownType() {
        assertEquals("bye, list, mark, unmark, delete, find, update, todo, deadline, event",
                CommandType.getCommandSummary());
    }

    @Test
    public void getKeyword_everyCommand_returnsConfiguredKeyword() {
        assertEquals("bye", CommandType.BYE.getKeyword());
        assertEquals("list", CommandType.LIST.getKeyword());
        assertEquals("mark", CommandType.MARK.getKeyword());
        assertEquals("unmark", CommandType.UNMARK.getKeyword());
        assertEquals("delete", CommandType.DELETE.getKeyword());
        assertEquals("find", CommandType.FIND.getKeyword());
        assertEquals("todo", CommandType.TODO.getKeyword());
        assertEquals("deadline", CommandType.DEADLINE.getKeyword());
        assertEquals("event", CommandType.EVENT.getKeyword());
        assertEquals("", CommandType.UNKNOWN.getKeyword());
    }

    @Test
    public void fromCommand_supportedCommands_returnsMatchingTypes() {
        assertEquals(CommandType.BYE, CommandType.fromCommand("bye"));
        assertEquals(CommandType.LIST, CommandType.fromCommand("list"));
        assertEquals(CommandType.MARK, CommandType.fromCommand("mark 2"));
        assertEquals(CommandType.UNMARK, CommandType.fromCommand("unmark 2"));
        assertEquals(CommandType.DELETE, CommandType.fromCommand("delete 2"));
        assertEquals(CommandType.FIND, CommandType.fromCommand("find book"));
        assertEquals(CommandType.UPDATE, CommandType.fromCommand("update 2 revised description"));
        assertEquals(CommandType.TODO, CommandType.fromCommand("todo read book"));
        assertEquals(CommandType.DEADLINE,
                CommandType.fromCommand("deadline return book /by 2026-08-30"));
        assertEquals(CommandType.EVENT,
                CommandType.fromCommand("event meeting /from 2026-09-01 /to 2026-09-02"));
    }

    @Test
    public void fromCommand_argumentCommandWithoutArguments_returnsMatchingType() {
        assertEquals(CommandType.MARK, CommandType.fromCommand("mark"));
        assertEquals(CommandType.FIND, CommandType.fromCommand("find"));
        assertEquals(CommandType.UPDATE, CommandType.fromCommand("update"));
        assertEquals(CommandType.TODO, CommandType.fromCommand("todo"));
        assertEquals(CommandType.DEADLINE, CommandType.fromCommand("deadline"));
    }

    @Test
    public void fromCommand_exactCommandWithArguments_returnsUnknown() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromCommand("bye now"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromCommand("list all"));
    }

    @Test
    public void fromCommand_invalidInput_returnsUnknown() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromCommand(""));
        assertEquals(CommandType.UNKNOWN, CommandType.fromCommand("TODO read book"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromCommand("marking 2"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromCommand("unknown command"));
    }

    @Test
    public void fromCommand_multipleSpacesAfterKeyword_returnsMatchingType() {
        assertEquals(CommandType.TODO, CommandType.fromCommand("todo   read book"));
        assertEquals(CommandType.MARK, CommandType.fromCommand("mark\t2"));
    }
}
