/**
 * Represents a task without an attached date or time.
 */
public class Todo extends Task {
    /**
     * Creates an incomplete todo task.
     *
     * @param description text describing the task
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Converts this todo into one line of saved data.
     *
     * @return saved todo data
     */
    @Override
    public String toDataString() {
        return "T | " + super.toDataString();
    }

    /**
     * Returns this task with the todo type indicator.
     *
     * @return the formatted todo task
     */
    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
