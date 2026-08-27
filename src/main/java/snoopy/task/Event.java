package snoopy.task;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a task that occurs between a start and end time.
 */
public class Event extends Task {
    /** Format used to present stored dates to the user. */
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy", Locale.ENGLISH);

    protected LocalDate from;
    protected LocalDate to;

    /**
     * Creates an incomplete event task.
     *
     * @param description text describing the event
     * @param from start date
     * @param to end date
     */
    public Event(String description, LocalDate from, LocalDate to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Converts this event into one line of saved data.
     *
     * @return saved event data
     */
    @Override
    public String toDataString() {
        return "E | " + super.toDataString() + " | " + this.from + " | " + this.to;
    }

    /**
     * Returns this task with its type, start time, and end time.
     *
     * @return the formatted event task
     */
    @Override
    public String toString() {
        return "[E]" + super.toString()
                + " (from: " + from.format(DISPLAY_DATE_FORMAT)
                + " to: " + to.format(DISPLAY_DATE_FORMAT) + ")";
    }
}
