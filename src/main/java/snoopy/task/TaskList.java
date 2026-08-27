package snoopy.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Owns Snoopy's collection of tasks and provides operations on that collection.
 */
public class TaskList implements Iterable<Task> {
    private final List<Task> tasks;

    /**
     * Creates an empty task list.
     */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing tasks loaded from storage.
     * A copy is made so that this class remains the sole owner of its collection.
     *
     * @param tasks tasks with which to initialise the list
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(tasks);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to add
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task at a zero-based index.
     *
     * @param index zero-based task index
     * @return task at the given index
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Removes and returns the task at a zero-based index.
     *
     * @param index zero-based task index
     * @return removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return current task count
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Returns tasks whose descriptions contain a case-sensitive keyword.
     * The matching tasks remain shared so their completion states are preserved.
     *
     * @param keyword keyword to find in task descriptions
     * @return a new list containing matching tasks in their original order
     */
    public TaskList find(String keyword) {
        TaskList matches = new TaskList();
        for (Task task : tasks) {
            if (task.containsKeyword(keyword)) {
                matches.add(task);
            }
        }
        return matches;
    }

    /**
     * Returns a read-only iterator for saving or displaying every task.
     *
     * @return iterator over tasks in list order
     */
    @Override
    public Iterator<Task> iterator() {
        return Collections.unmodifiableList(tasks).iterator();
    }
}
