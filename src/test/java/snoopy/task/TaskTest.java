package snoopy.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

/**
 * Tests the completion state and common saved/display formats of tasks.
 */
public class TaskTest {
    @Test
    public void completionState_markAndUnmark_updatesAllRepresentations() {
        Task task = new Task("read book");

        assertEquals(" ", task.getStatusIcon());
        assertEquals("0 | read book", task.toDataString());
        assertEquals("[ ] read book", task.toString());

        task.markAsDone();
        assertEquals("X", task.getStatusIcon());
        assertEquals("1 | read book", task.toDataString());
        assertEquals("[X] read book", task.toString());

        task.markAsNotDone();
        assertEquals(" ", task.getStatusIcon());
        assertEquals("0 | read book", task.toDataString());
        assertEquals("[ ] read book", task.toString());
    }

    @Test
    public void todo_formatsIncludeTaskTypeAndState() {
        Todo todo = new Todo("read book");

        assertEquals("T | 0 | read book", todo.toDataString());
        assertEquals("[T][ ] read book", todo.toString());

        todo.markAsDone();
        assertEquals("T | 1 | read book", todo.toDataString());
        assertEquals("[T][X] read book", todo.toString());
    }

    @Test
    public void containsKeyword_exactSubstring_matchesDescriptionOnly() {
        Task task = new Deadline("return Book", LocalDate.of(2026, 8, 30));

        assertTrue(task.containsKeyword("return"));
        assertTrue(task.containsKeyword("Book"));
        assertFalse(task.containsKeyword("book"));
        assertFalse(task.containsKeyword("Aug 30"));
    }
}
