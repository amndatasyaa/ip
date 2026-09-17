package snoopy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import snoopy.storage.Storage;
import snoopy.task.Task;

/**
 * Tests the response API shared by Snoopy's text and graphical interfaces.
 */
public class SnoopyTest {
    @TempDir
    private Path temporaryDirectory;

    @Test
    public void getResponse_addMarkAndList_updatesSharedTaskState() {
        Snoopy snoopy = createSnoopy("commands.txt");

        assertEquals(" Got it. I've added this task:\n"
                        + "   [T][ ] read book\n"
                        + " Now you have 1 tasks in the list.",
                snoopy.getResponse("todo read book"));
        assertEquals(" Nice! I've marked this task as done:\n"
                        + "   [T][X] read book",
                snoopy.getResponse("mark 1"));
        assertEquals(" Here are the tasks in your list:\n"
                        + " 1.[T][X] read book",
                snoopy.getResponse("list"));
    }

    @Test
    public void getResponse_invalidCommand_preservesExistingTaskState() {
        Snoopy snoopy = createSnoopy("invalid-command.txt");
        snoopy.getResponse("todo anchor");

        assertEquals(" OOPS! Please enter the deadline date as yyyy-MM-dd, "
                        + "for example 2019-10-15.",
                snoopy.getResponse("deadline impossible /by 2026-02-30"));
        assertEquals(" Here are the tasks in your list:\n"
                        + " 1.[T][ ] anchor",
                snoopy.getResponse("list"));
    }

    @Test
    public void getResponse_eventEndingBeforeStart_rejectsEventAndPreservesState() {
        Snoopy snoopy = createSnoopy("reversed-event.txt");
        snoopy.getResponse("todo anchor");

        assertEquals(" OOPS! The event end date cannot be before its start date.",
                snoopy.getResponse("event holiday /from 2026-09-10 /to 2026-09-09"));
        assertEquals(" Here are the tasks in your list:\n"
                        + " 1.[T][ ] anchor",
                snoopy.getResponse("list"));
    }

    @Test
    public void getResponse_repeatedDelimitersAndEqualDates_preservesExistingState() {
        Snoopy snoopy = createSnoopy("repeated-delimiters.txt");
        snoopy.getResponse("todo anchor");

        assertEquals(" OOPS! A deadline must contain exactly one '/by' separator.",
                snoopy.getResponse("deadline report /by 2026-09-01 /by 2026-09-02"));
        assertEquals(" OOPS! An event must contain exactly one '/from' and one '/to' separator.",
                snoopy.getResponse(
                        "event trip /from 2026-09-01 /from 2026-09-02 /to 2026-09-03"));
        assertEquals(" OOPS! The event end date must be after its start date.",
                snoopy.getResponse("event one-day /from 2026-09-01 /to 2026-09-01"));
        assertEquals(" Here are the tasks in your list:\n"
                        + " 1.[T][ ] anchor",
                snoopy.getResponse("list"));
    }

    @Test
    public void getResponse_saveFailure_rollsBackEveryMutation() {
        FailingStorage storage = new FailingStorage(temporaryDirectory.resolve("save-failure.txt"));
        Snoopy snoopy = new Snoopy(storage);
        snoopy.getResponse("todo first");
        snoopy.getResponse("todo second");
        snoopy.getResponse("mark 1");
        storage.failSaves();

        String saveError = " OOPS! I couldn't save the task list.";
        assertEquals(saveError, snoopy.getResponse("todo third"));
        assertEquals(saveError, snoopy.getResponse("mark 2"));
        assertEquals(saveError, snoopy.getResponse("unmark 1"));
        assertEquals(saveError, snoopy.getResponse("delete 1"));
        assertEquals(" Here are the tasks in your list:\n"
                        + " 1.[T][X] first\n"
                        + " 2.[T][ ] second",
                snoopy.getResponse("list"));
    }

    @Test
    public void getResponse_bye_requestsApplicationExit() {
        Snoopy snoopy = createSnoopy("bye.txt");

        assertFalse(snoopy.shouldExit());
        assertEquals(" Bye. Hope to see you again soon!", snoopy.getResponse("bye"));
        assertTrue(snoopy.shouldExit());
    }

    @Test
    public void getWelcomeMessage_corruptedStorage_includesSafeRecoveryMessage() throws IOException {
        Path dataFile = temporaryDirectory.resolve("corrupted.txt");
        Files.writeString(dataFile, "D | 0 | invalid | 2026-02-30\n");

        Snoopy snoopy = new Snoopy(new Storage(dataFile));

        assertEquals("Hi! I'm Snoopy, your happy little helper.\n"
                        + "What can I do for you?\n\n"
                        + "OOPS! The data file is corrupted at line 1.",
                snoopy.getWelcomeMessage());
    }

    /**
     * Creates Snoopy with an isolated data file for one test.
     *
     * @param fileName Name of the temporary data file.
     * @return Snoopy backed by isolated storage.
     */
    private Snoopy createSnoopy(String fileName) {
        return new Snoopy(new Storage(temporaryDirectory.resolve(fileName)));
    }

    /**
     * Storage test double that can simulate a write failure after setup.
     */
    private static class FailingStorage extends Storage {
        private boolean shouldFail;

        FailingStorage(Path filePath) {
            super(filePath);
        }

        void failSaves() {
            shouldFail = true;
        }

        @Override
        public void save(ArrayList<Task> tasks) throws IOException {
            if (shouldFail) {
                throw new IOException("Simulated write failure");
            }
            super.save(tasks);
        }
    }
}
