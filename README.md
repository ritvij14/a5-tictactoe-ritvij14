# Tic Tac Toe

This is a starter code for the Tic Tac Toe multiplayer game app assignment.

It uses Android Navigation Component, with a single activity and three fragments:

- The DashboardFragment is the home screen. If a user is not logged in, it should navigate to the
  LoginFragment. (See the TODO comment in code.)

- The floating button in the dashboard creates a dialog that asks which type of game to create and
  passes that information to the GameFragment (using SafeArgs).

- The GameFragment UI has a 3x3 grid of buttons. They are initialized in the starter code.
  Appropriate listeners and game play logic needs to be provided.

- Pressing the back button in the GameFragment opens a dialog that confirms if the user wants to
  forfeit the game. (See the FIXME comment in code.)

- A "log out" action bar menu is shown on both the dashboard and the game fragments. Clicking it
  should log the user out and show the LoginFragment. This click is handled in the MainActivity.

### a. Basic Information

**Name of the Project** - Tic Tac Toe

**Name of Student** - Ritvij Kumar Sharma

**BITS ID** - 2019A8PS0666G

**Email** - f20190666@goa.bits-pilani.ac.in

### b. What does the app do? Any known bugs?

This app simulates a Tic Tac Toe game between two player or a single player on a device. The games
and their data are recorded in Firebase. Right now any player cqan enter a game to play against you

### c. Description of completed tasks and steps followed to achieve them

**Task 1 - Implementing Sign-in Screen**

- Initialising Firebase object, and pushing user data to add user.
- User can log in with any email and password, and can then view active games currently present in
  the database.

**Task 2 - Implementing Single-Player Mode**

- After user logs in, they can choose a single player game.
- For the single player game, the computer will choose any block randomly after the player's turn.

**Task 3 - Implementing Two-Player Mode**

- After user logs in, and chooses a two player game, a new game entry is created in the database.
- Two users can play together where the tic tac toe grid is tracked through an array.

### d. Testing using written test cases and monkey stress-testing

- During monkey testing, app crashed only due to denial of the
  permission `android.permission.BROADCAST_CLOSE_SYSTEM_DIALOGS`. This is a protected service for
  system apps so I am unsure of how to implement it.

### e. Approximate number of hours it took to complete the assignment

**Writing Code, Testing and Solving Accessibility Issues** -> 16 hours

**Documentation** -> 1 hour

**Total Time = 17 hours**

### f. Difficulty of Assignment

On a scale of 1 to 10, I would rate the difficulty of the assignment as **9**.
