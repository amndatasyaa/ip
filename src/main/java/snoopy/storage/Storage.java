package snoopy.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import snoopy.exception.SnoopyException;
import snoopy.task.Deadline;
import snoopy.task.Event;
import snoopy.task.Task;
import snoopy.task.TaskList;
import snoopy.task.Todo;

/**
 * Saves and loads Snoopy's tasks using a text file on the hard disk.
 */
public class Storage {
    private final Path filePath;

    /**
     * Creates storage that uses Snoopy's standard data-file location.
     */
    public Storage() {
        this(Path.of("data", "snoopy.txt"));
    }

    /**
     * Creates storage at a specified path so persistence can be tested in isolation.
     *
     * @param filePath location of the data file
     */
    Storage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Replaces the data file with the current task list.
     *
     * @param tasks current tasks to save
     * @throws IOException if the folder or file cannot be written
     */
    public void save(TaskList tasks) throws IOException {
        ArrayList<String> lines = new ArrayList<>();
        for (Task task : tasks) {
            lines.add(task.toDataString());
        }
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, lines);
    }

    /**
     * Loads all tasks from the data file, or returns an empty list on first use.
     *
     * @return tasks reconstructed from the data file
     * @throws IOException if an existing data file cannot be read
     * @throws SnoopyException if a saved line does not follow the expected format
     */
    public ArrayList<Task> load() throws IOException, SnoopyException {
        ArrayList<Task> tasks = new ArrayList<>();
        if (!Files.exists(filePath)) {
            return tasks;
        }

        ArrayList<String> lines = new ArrayList<>(Files.readAllLines(filePath));
        for (int i = 0; i < lines.size(); i++) {
            tasks.add(parseTask(lines.get(i), i + 1));
        }
        return tasks;
    }

    /**
     * Reconstructs one task from a line in the data file.
     *
     * @param line saved task data
     * @param lineNumber one-based line number used in corruption messages
     * @return reconstructed task
     * @throws SnoopyException if the saved data is malformed
     */
    private Task parseTask(String line, int lineNumber) throws SnoopyException {
        String[] fields = line.split(" \\| ", -1);
        if (fields.length < 3 || !(fields[1].equals("0") || fields[1].equals("1"))) {
            throw corruptedFileException(lineNumber);
        }

        Task task;
        switch (fields[0]) {
        case "T":
            if (fields.length != 3 || fields[2].isBlank()) {
                throw corruptedFileException(lineNumber);
            }
            task = new Todo(fields[2]);
            break;
        case "D":
            if (fields.length != 4 || fields[2].isBlank() || fields[3].isBlank()) {
                throw corruptedFileException(lineNumber);
            }
            task = createDeadline(fields, lineNumber);
            break;
        case "E":
            if (fields.length != 5 || fields[2].isBlank()
                    || fields[3].isBlank() || fields[4].isBlank()) {
                throw corruptedFileException(lineNumber);
            }
            task = createEvent(fields, lineNumber);
            break;
        default:
            throw corruptedFileException(lineNumber);
        }

        if (fields[1].equals("1")) {
            task.markAsDone();
        }
        return task;
    }

    /**
     * Reconstructs a deadline and validates its saved ISO date.
     *
     * @param fields fields from one saved deadline
     * @param lineNumber one-based location of the saved record
     * @return reconstructed deadline
     * @throws SnoopyException if the saved date is invalid
     */
    private Deadline createDeadline(String[] fields, int lineNumber) throws SnoopyException {
        try {
            return new Deadline(fields[2], LocalDate.parse(fields[3]));
        } catch (DateTimeParseException exception) {
            throw corruptedFileException(lineNumber);
        }
    }

    /**
     * Reconstructs an event and validates its saved ISO dates.
     *
     * @param fields fields from one saved event
     * @param lineNumber one-based location of the saved record
     * @return reconstructed event
     * @throws SnoopyException if either saved date is invalid
     */
    private Event createEvent(String[] fields, int lineNumber) throws SnoopyException {
        try {
            return new Event(fields[2], LocalDate.parse(fields[3]), LocalDate.parse(fields[4]));
        } catch (DateTimeParseException exception) {
            throw corruptedFileException(lineNumber);
        }
    }

    /**
     * Creates a consistent error for malformed saved data.
     *
     * @param lineNumber one-based location of the malformed record
     * @return exception describing the corrupted line
     */
    private SnoopyException corruptedFileException(int lineNumber) {
        return new SnoopyException("The data file is corrupted at line " + lineNumber + ".");
    }
}
