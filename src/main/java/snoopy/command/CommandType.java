package snoopy.command;

/**
 * Represents the commands understood by Snoopy.
 */
public enum CommandType {
    BYE("bye"),
    LIST("list"),
    MARK("mark"),
    UNMARK("unmark"),
    DELETE("delete"),
    FIND("find"),
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    UNKNOWN("");

    private final String keyword;

    /**
     * Creates a command type associated with its user-facing keyword.
     *
     * @param keyword first word used to invoke the command
     */
    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the keyword used to invoke this command.
     *
     * @return command keyword
     */
    public String getKeyword() {
        return keyword;
    }

}
