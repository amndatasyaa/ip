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
    TODO("todo"),
    DEADLINE("deadline"),
    EVENT("event"),
    UNKNOWN("");

    private final String keyword;

    /**
     * Creates a command type associated with its user-facing keyword.
     *
     * @param keyword First word used to invoke the command.
     */
    CommandType(String keyword) {
        this.keyword = keyword;
    }

    /**
     * Returns the keyword used to invoke this command.
     *
     * @return Command keyword.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Identifies a command from the first word of the user's input.
     *
     * @param command Complete trimmed input from the user.
     * @return Matching command type, or {@link #UNKNOWN} when no command matches.
     */
    public static CommandType fromCommand(String command) {
        if (command.isEmpty()) {
            return UNKNOWN;
        }

        String firstWord = command.split("\\s+", 2)[0];
        for (CommandType type : values()) {
            if (type.keyword.equals(firstWord)) {
                boolean acceptsArguments = type != BYE && type != LIST;
                if (!acceptsArguments && !command.equals(type.keyword)) {
                    return UNKNOWN;
                }
                return type;
            }
        }
        return UNKNOWN;
    }
}
