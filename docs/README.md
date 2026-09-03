# Snoopy User Guide

Snoopy is a desktop chatbot that helps you record and manage todos, deadlines, and events using short
text commands.

## Starting Snoopy

Run `./gradlew run` from the project folder. Type a command in the field at the bottom of the window,
then press **Enter** or click **Send**.

## Adding todos

Use `todo <description>` to add a task without a date.

Example: `todo borrow book`

## Adding deadlines

Use `deadline <description> /by <yyyy-MM-dd>` to add a task with a due date.

Example: `deadline return book /by 2026-09-10`

## Adding events

Use `event <description> /from <yyyy-MM-dd> /to <yyyy-MM-dd>` to add an event with start and end dates.

Example: `event project meeting /from 2026-09-10 /to 2026-09-11`

## Managing tasks

- `list` shows all tasks.
- `mark <number>` marks a task as complete.
- `unmark <number>` marks it as incomplete.
- `delete <number>` removes it.
- `find <keyword>` searches task descriptions.

## Exiting

Enter `bye` to close Snoopy after its farewell message. You can also close the application using the
window controls.
