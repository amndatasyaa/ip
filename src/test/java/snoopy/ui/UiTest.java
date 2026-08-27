package snoopy.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import org.junit.jupiter.api.Test;

import snoopy.task.TaskList;
import snoopy.task.Todo;

/**
 * Tests console input normalization and representative UI responses.
 */
public class UiTest {
    @Test
    public void readCommand_surroundingWhitespace_returnsTrimmedCommand() {
        Ui ui = createUi("   todo read book   \n", new ByteArrayOutputStream());

        assertTrue(ui.hasNextCommand());
        assertEquals("todo read book", ui.readCommand());
        assertFalse(ui.hasNextCommand());
    }

    @Test
    public void showTaskList_multipleTasks_printsNumberedTasksInOrder() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = createUi("", output);
        TaskList tasks = new TaskList();
        tasks.add(new Todo("first"));
        tasks.add(new Todo("second"));

        ui.showTaskList(tasks);

        assertEquals(" Here are the tasks in your list:\n"
                + " 1.[T][ ] first\n"
                + " 2.[T][ ] second\n", output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showError_message_printsStandardErrorPrefix() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = createUi("", output);

        ui.showError("Something went wrong.");

        assertEquals(" OOPS! Something went wrong.\n",
                output.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void showMatchingTasks_matches_printsMatchingHeadingAndRenumberedTasks() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Ui ui = createUi("", output);
        TaskList matches = new TaskList();
        matches.add(new Todo("first match"));

        ui.showMatchingTasks(matches);

        assertEquals(" Here are the matching tasks in your list:\n"
                + " 1.[T][ ] first match\n", output.toString(StandardCharsets.UTF_8));
    }

    private Ui createUi(String input, ByteArrayOutputStream output) {
        Scanner scanner = new Scanner(input);
        PrintStream printStream = new PrintStream(output, true, StandardCharsets.UTF_8);
        return new Ui(scanner, printStream);
    }
}
