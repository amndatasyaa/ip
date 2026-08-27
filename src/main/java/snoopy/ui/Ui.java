package snoopy.ui;

import java.io.PrintStream;
import java.util.Scanner;

import snoopy.task.Task;
import snoopy.task.TaskList;

/**
 * Handles all console input and output for the Snoopy chatbot.
 */
public class Ui {
    private static final String DIVIDER =
            "____________________________________________________________";
    private static final String BANNER = "  ____\n"
            + " / ___| _ __   ___   ___  _ __  _   _\n"
            + " \\___ \\| '_ \\ / _ \\ / _ \\| '_ \\| | | |\n"
            + "  ___) | | | | (_) | (_) | |_) | |_| |\n"
            + " |____/|_| |_|\\___/ \\___/| .__/ \\__, |\n"
            + "                            |_|    |___/";

    private final Scanner scanner;
    private final PrintStream output;

    /**
     * Creates a UI connected to the program's standard input and output.
     */
    public Ui() {
        this(new Scanner(System.in), System.out);
    }

    /**
     * Creates a UI with specified streams so its behaviour can be tested.
     *
     * @param scanner source of user commands
     * @param output destination for chatbot messages
     */
    Ui(Scanner scanner, PrintStream output) {
        this.scanner = scanner;
        this.output = output;
    }

    /**
     * Returns whether another command is available from the user.
     *
     * @return true when another command can be read
     */
    public boolean hasNextCommand() {
        return scanner.hasNextLine();
    }

    /**
     * Reads the next command and removes surrounding whitespace.
     *
     * @return next normalized command
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /**
     * Displays Snoopy's greeting and command prompt.
     */
    public void showWelcome() {
        output.println(DIVIDER);
        output.println(BANNER);
        output.println("Hi! I'm Snoopy, your happy little helper.");
        output.println("What can I do for you?");
        showLine();
    }

    /**
     * Displays the divider between responses.
     */
    public void showLine() {
        output.println(DIVIDER);
    }

    /**
     * Displays an input or data error that Snoopy can explain.
     *
     * @param message user-friendly explanation
     */
    public void showError(String message) {
        output.println(" OOPS! " + message);
    }

    /**
     * Displays the error used when saved tasks cannot be read.
     */
    public void showLoadingError() {
        showError("I couldn't load the saved tasks.");
    }

    /**
     * Displays the error used when tasks cannot be saved.
     */
    public void showSavingError() {
        showError("I couldn't save the task list.");
    }

    /**
     * Displays all tasks in their current list order.
     *
     * @param tasks tasks to display
     */
    public void showTaskList(TaskList tasks) {
        output.println(" Here are the tasks in your list:");
        for (int i = 0; i < tasks.size(); i++) {
            output.println(" " + (i + 1) + "." + tasks.get(i));
        }
    }

    /**
     * Confirms that a task was marked as done.
     *
     * @param task updated task
     */
    public void showTaskMarked(Task task) {
        output.println(" Nice! I've marked this task as done:");
        output.println("   " + task);
    }

    /**
     * Confirms that a task was marked as not done.
     *
     * @param task updated task
     */
    public void showTaskUnmarked(Task task) {
        output.println(" OK, I've marked this task as not done yet:");
        output.println("   " + task);
    }

    /**
     * Confirms that a task was added.
     *
     * @param task added task
     * @param taskCount current number of tasks
     */
    public void showTaskAdded(Task task, int taskCount) {
        output.println(" Got it. I've added this task:");
        output.println("   " + task);
        showTaskCount(taskCount);
    }

    /**
     * Confirms that a task was removed.
     *
     * @param task removed task
     * @param taskCount current number of tasks
     */
    public void showTaskDeleted(Task task, int taskCount) {
        output.println(" Noted. I've removed this task:");
        output.println("   " + task);
        showTaskCount(taskCount);
    }

    /**
     * Displays Snoopy's farewell.
     */
    public void showGoodbye() {
        output.println(" Bye. Hope to see you again soon!");
    }

    /**
     * Displays the current task count after an addition or deletion.
     *
     * @param taskCount current number of tasks
     */
    private void showTaskCount(int taskCount) {
        output.println(" Now you have " + taskCount + " tasks in the list.");
    }
}
