package snoopy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import snoopy.storage.Storage;

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
}
