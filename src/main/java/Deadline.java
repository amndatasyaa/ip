/**
 * Represents a task that must be completed by a specific time.
 */
public class Deadline extends Task {
    protected String by;

    /**
     * Creates an incomplete deadline task.
     *
     * @param description text describing the task
     * @param by deadline text supplied by the user
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Converts this deadline into one line of saved data.
     *
     * @return saved deadline data
     */
    @Override
    public String toDataString() {
        return "D | " + super.toDataString() + " | " + this.by;
    }

    /**
     * Returns this task with its type and deadline.
     *
     * @return the formatted deadline task
     */
    @Override
    public String toString() {
        return "[D]" + super.toString() + " (by: " + by + ")";
    }
}
