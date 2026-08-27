import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests saving, loading, and validation of persisted tasks.
 */
public class StorageTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void load_missingDataFile_returnsEmptyList() throws IOException, SnoopyException {
        Storage storage = new Storage(temporaryDirectory.resolve("data/snoopy.txt"));

        assertEquals(new ArrayList<>(), storage.load());
    }

    @Test
    public void saveAndLoad_allTaskTypes_preservesOrderDetailsAndStatus()
            throws IOException, SnoopyException {
        Path dataFile = temporaryDirectory.resolve("nested/data/snoopy.txt");
        Storage storage = new Storage(dataFile);
        ArrayList<Task> originalTasks = new ArrayList<>();
        originalTasks.add(new Todo("read book"));
        originalTasks.add(new Deadline("return book", LocalDate.of(2026, 8, 30)));
        originalTasks.add(new Event("meeting", LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 2)));
        originalTasks.get(1).markAsDone();

        storage.save(originalTasks);
        ArrayList<Task> loadedTasks = storage.load();

        assertEquals(3, loadedTasks.size());
        assertInstanceOf(Todo.class, loadedTasks.get(0));
        assertInstanceOf(Deadline.class, loadedTasks.get(1));
        assertInstanceOf(Event.class, loadedTasks.get(2));
        assertEquals("T | 0 | read book", loadedTasks.get(0).toDataString());
        assertEquals("D | 1 | return book | 2026-08-30", loadedTasks.get(1).toDataString());
        assertEquals("E | 0 | meeting | 2026-09-01 | 2026-09-02",
                loadedTasks.get(2).toDataString());
        assertEquals(originalTasks.get(0).toString(), loadedTasks.get(0).toString());
        assertEquals(originalTasks.get(1).toString(), loadedTasks.get(1).toString());
        assertEquals(originalTasks.get(2).toString(), loadedTasks.get(2).toString());
    }

    @Test
    public void load_corruptedSecondLine_reportsLineNumber() throws IOException {
        Path dataFile = temporaryDirectory.resolve("snoopy.txt");
        Files.writeString(dataFile, "T | 0 | valid task\nD | 0 | bad date | 2026-02-30\n");
        Storage storage = new Storage(dataFile);

        SnoopyException exception = assertThrows(SnoopyException.class, storage::load);

        assertEquals("The data file is corrupted at line 2.", exception.getMessage());
    }

    @Test
    public void load_invalidRecordShapes_rejectsEachRecord() {
        String[] invalidRecords = {
            "T | 2 | invalid status",
            "X | 0 | unknown type",
            "T | 0 | ",
            "D | 0 | missing date",
            "E | 0 | meeting | 2026-09-01",
            "E | 0 | meeting | invalid | 2026-09-02"
        };

        for (int i = 0; i < invalidRecords.length; i++) {
            Path dataFile = temporaryDirectory.resolve("invalid-" + i + ".txt");
            try {
                Files.writeString(dataFile, invalidRecords[i]);
            } catch (IOException exception) {
                throw new AssertionError("Test setup could not write a temporary data file", exception);
            }
            Storage storage = new Storage(dataFile);

            SnoopyException exception = assertThrows(SnoopyException.class, storage::load,
                    "Expected record to be rejected: " + invalidRecords[i]);
            assertEquals("The data file is corrupted at line 1.", exception.getMessage());
        }
    }
}
