# Snoopy UI Test Plan

The tests compare the complete console output exactly, including whitespace and dividers. Each test starts a fresh Snoopy process.

## UI-01: Add and list all task types

**Aim:** Verify that todos, deadlines, and events are created and displayed using their type-specific formats.

### Input

```text
todo borrow book
deadline return book /by Sunday
event project meeting /from Mon 2pm /to 4pm
list
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 Got it. I've added this task:
   [T][ ] borrow book
 Now you have 1 tasks in the list.
____________________________________________________________
 Got it. I've added this task:
   [D][ ] return book (by: Sunday)
 Now you have 2 tasks in the list.
____________________________________________________________
 Got it. I've added this task:
   [E][ ] project meeting (from: Mon 2pm to: 4pm)
 Now you have 3 tasks in the list.
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] borrow book
 2.[D][ ] return book (by: Sunday)
 3.[E][ ] project meeting (from: Mon 2pm to: 4pm)
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## UI-02: Mark and unmark a task

**Aim:** Verify that a task's done status can be set and then reversed without changing its description or type.

### Input

```text
todo read book
mark 1
unmark 1
list
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] read book
____________________________________________________________
 OK, I've marked this task as not done yet:
   [T][ ] read book
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] read book
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## UI-03: Reject an empty todo and an unknown command

**Aim:** Verify that the chatbot explains the two minimum required input errors and remains available for the next command.

### Input

```text
todo
blah
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 OOPS! Please tell me what to add after 'todo'.
____________________________________________________________
 OOPS! Sorry, I don't recognize that command. Try todo, deadline, event, list, mark, or unmark.
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## UI-04: Reject malformed task details and task numbers

**Aim:** Verify that malformed deadlines, events, and task numbers produce specific errors without terminating the chatbot.

### Input

```text
deadline return book
deadline /by Sunday
event meeting /from Monday
event meeting /from /to Tuesday
mark
mark abc
mark 1
todo read book
mark 2
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 OOPS! Please use: deadline <description> /by <date or time>.
____________________________________________________________
 OOPS! A deadline needs both a description and a '/by' value.
____________________________________________________________
 OOPS! Please use: event <description> /from <start> /to <end>.
____________________________________________________________
 OOPS! An event needs a description, a '/from' value, and a '/to' value.
____________________________________________________________
 OOPS! Please provide a task number, for example 'mark 2'.
____________________________________________________________
 OOPS! 'abc' is not a valid task number.
____________________________________________________________
 OOPS! Your task list is empty, so there is nothing to mark or unmark.
____________________________________________________________
 Got it. I've added this task:
   [T][ ] read book
 Now you have 1 tasks in the list.
____________________________________________________________
 OOPS! Task 2 does not exist. Choose a number from 1 to 1.
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## UI-05: Rejected additions preserve task order and count

**Aim:** Verify that invalid todo, deadline, and event commands do not consume task slots or alter the order of valid tasks added before and after them.

### Input

```text
todo alpha
deadline beta /by Friday
todo
deadline gamma
event delta /from Monday
event meeting /from Mon /to Tue
list
deadline /by Sunday
todo epsilon
list
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 Got it. I've added this task:
   [T][ ] alpha
 Now you have 1 tasks in the list.
____________________________________________________________
 Got it. I've added this task:
   [D][ ] beta (by: Friday)
 Now you have 2 tasks in the list.
____________________________________________________________
 OOPS! Please tell me what to add after 'todo'.
____________________________________________________________
 OOPS! Please use: deadline <description> /by <date or time>.
____________________________________________________________
 OOPS! Please use: event <description> /from <start> /to <end>.
____________________________________________________________
 Got it. I've added this task:
   [E][ ] meeting (from: Mon to: Tue)
 Now you have 3 tasks in the list.
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] alpha
 2.[D][ ] beta (by: Friday)
 3.[E][ ] meeting (from: Mon to: Tue)
____________________________________________________________
 OOPS! A deadline needs both a description and a '/by' value.
____________________________________________________________
 Got it. I've added this task:
   [T][ ] epsilon
 Now you have 4 tasks in the list.
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] alpha
 2.[D][ ] beta (by: Friday)
 3.[E][ ] meeting (from: Mon to: Tue)
 4.[T][ ] epsilon
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## UI-06: Rejected status changes preserve completion states

**Aim:** Verify that invalid mark and unmark commands do not change existing task statuses and that later valid status changes still target the correct tasks.

### Input

```text
todo first
todo second
mark 1
mark abc
mark 3
unmark 2
list
unmark -1
unmark 1
mark 2
list
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 Got it. I've added this task:
   [T][ ] first
 Now you have 1 tasks in the list.
____________________________________________________________
 Got it. I've added this task:
   [T][ ] second
 Now you have 2 tasks in the list.
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] first
____________________________________________________________
 OOPS! 'abc' is not a valid task number.
____________________________________________________________
 OOPS! Task 3 does not exist. Choose a number from 1 to 2.
____________________________________________________________
 OK, I've marked this task as not done yet:
   [T][ ] second
____________________________________________________________
 Here are the tasks in your list:
 1.[T][X] first
 2.[T][ ] second
____________________________________________________________
 OOPS! Task -1 does not exist. Choose a number from 1 to 2.
____________________________________________________________
 OK, I've marked this task as not done yet:
   [T][ ] first
____________________________________________________________
 Nice! I've marked this task as done:
   [T][X] second
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] first
 2.[T][X] second
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```

## UI-07: Whitespace and incomplete suffixes do not corrupt state

**Aim:** Verify that surrounding whitespace is handled consistently while incomplete command suffixes and case-sensitive unknown commands leave the valid task unchanged.

### Input

```text

   todo spaced task
deadline report /by
event review /from Mon /to
mark 1 extra
LIST
list
bye
```

### Expected output

```text
____________________________________________________________
  ____
 / ___| _ __   ___   ___  _ __  _   _
 \___ \| '_ \ / _ \ / _ \| '_ \| | | |
  ___) | | | | (_) | (_) | |_) | |_| |
 |____/|_| |_|\___/ \___/| .__/ \__, |
                            |_|    |___/
Hi! I'm Snoopy, your happy little helper.
What can I do for you?
____________________________________________________________
 OOPS! Sorry, I don't recognize that command. Try todo, deadline, event, list, mark, or unmark.
____________________________________________________________
 Got it. I've added this task:
   [T][ ] spaced task
 Now you have 1 tasks in the list.
____________________________________________________________
 OOPS! Please use: deadline <description> /by <date or time>.
____________________________________________________________
 OOPS! Please use: event <description> /from <start> /to <end>.
____________________________________________________________
 OOPS! '1 extra' is not a valid task number.
____________________________________________________________
 OOPS! Sorry, I don't recognize that command. Try todo, deadline, event, list, mark, or unmark.
____________________________________________________________
 Here are the tasks in your list:
 1.[T][ ] spaced task
____________________________________________________________
 Bye. Hope to see you again soon!
____________________________________________________________
```
