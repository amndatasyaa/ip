package snoopy;

import java.io.IOException;

import snoopy.command.CommandType;
import snoopy.exception.SnoopyException;
import snoopy.parser.Parser;
import snoopy.storage.Storage;
import snoopy.task.Task;
import snoopy.task.TaskList;
import snoopy.ui.Ui;

/**
 * Starts the Snoopy chatbot application.
 */
public class Snoopy {
    private final Storage storage;
    private final Ui ui;
    private TaskList tasks;

    /**
     * Creates a chatbot using the standard console and data file.
     */
    public Snoopy() {
        this.storage = new Storage();
        this.ui = new Ui();
    }

    /**
     * Runs the chatbot until the user enters {@code bye}.
     *
     * @param args command-line arguments; not used by this application
     */
    public static void main(String[] args) {
        new Snoopy().run();
    }

    /**
     * Runs the chatbot until the user enters {@code bye} or input ends.
     */
    public void run() {
        ui.showWelcome();
        try {
            tasks = new TaskList(storage.load());
        } catch (SnoopyException exception) {
            tasks = new TaskList();
            ui.showError(exception.getMessage());
            ui.showLine();
        } catch (IOException exception) {
            tasks = new TaskList();
            ui.showLoadingError();
            ui.showLine();
        }

        chatLoop:
        while (ui.hasNextCommand()) {
            String command = ui.readCommand();
            CommandType commandType = Parser.parseCommandType(command);

            try {
                switch (commandType) {
                case BYE:
                    ui.showGoodbye();
                    ui.showLine();
                    break chatLoop;
                case LIST:
                    ui.showTaskList(tasks);
                    break;
                case UNMARK:
                    int taskIndex = Parser.parseTaskIndex(command, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsNotDone();
                    storage.save(tasks);
                    ui.showTaskUnmarked(tasks.get(taskIndex));
                    break;
                case MARK:
                    taskIndex = Parser.parseTaskIndex(command, tasks.size(), commandType);
                    tasks.get(taskIndex).markAsDone();
                    storage.save(tasks);
                    ui.showTaskMarked(tasks.get(taskIndex));
                    break;
                case DELETE:
                    taskIndex = Parser.parseTaskIndex(command, tasks.size(), commandType);
                    Task removedTask = tasks.remove(taskIndex);
                    storage.save(tasks);
                    ui.showTaskDeleted(removedTask, tasks.size());
                    break;
                case TODO:
                case DEADLINE:
                case EVENT:
                    Task task = Parser.parseTask(command, commandType);
                    tasks.add(task);
                    storage.save(tasks);
                    ui.showTaskAdded(task, tasks.size());
                    break;
                case UNKNOWN:
                    throw new SnoopyException(
                            "Sorry, I don't recognize that command. "
                                    + "Try todo, deadline, event, list, mark, unmark, or delete.");
                }
            } catch (SnoopyException exception) {
                ui.showError(exception.getMessage());
            } catch (IOException exception) {
                ui.showSavingError();
            }
            ui.showLine();
        }
    }

}
