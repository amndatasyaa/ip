package snoopy.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Iterator;

import org.junit.jupiter.api.Test;

/**
 * Tests ownership and mutation operations of the task collection.
 */
public class TaskListTest {
    @Test
    public void constructor_sourceListChanges_doesNotChangeTaskList() {
        ArrayList<Task> source = new ArrayList<>();
        source.add(new Todo("original"));
        TaskList tasks = new TaskList(source);

        source.clear();

        assertEquals(1, tasks.size());
        assertEquals("[T][ ] original", tasks.get(0).toString());
    }

    @Test
    public void addAndRemove_multipleTasks_preservesOrderAndReturnsRemovedTask() {
        Task first = new Todo("first");
        Task second = new Todo("second");
        TaskList tasks = new TaskList();

        tasks.add(first);
        tasks.add(second);
        Task removed = tasks.remove(0);

        assertSame(first, removed);
        assertEquals(1, tasks.size());
        assertSame(second, tasks.get(0));
    }

    @Test
    public void iterator_removeOperation_isRejected() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("protected"));
        Iterator<Task> iterator = tasks.iterator();
        iterator.next();

        assertThrows(UnsupportedOperationException.class, iterator::remove);
        assertEquals(1, tasks.size());
    }

    @Test
    public void find_matchingDescriptions_returnsMatchesInOriginalOrder() {
        TaskList tasks = new TaskList();
        tasks.add(new Todo("read book"));
        tasks.add(new Todo("call Alice"));
        tasks.add(new Todo("return book"));

        TaskList matches = tasks.find("book");

        assertEquals(2, matches.size());
        assertEquals("[T][ ] read book", matches.get(0).toString());
        assertEquals("[T][ ] return book", matches.get(1).toString());
        assertEquals(3, tasks.size());
    }
}
