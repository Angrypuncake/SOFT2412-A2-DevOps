# SOFT2412-A2
Notes from Elvis
What I Have Done So Far

Initial Project Structure Set up the JavaFX project with a clear folder structure. Added a basic model for User, AdminUser, GuestUser, and Scroll. Created a database connection class for future PostgreSQL/SQLite integration. Designed the Register.fxml file for user registration.
User Management: Defined core user types using an enum (UserType), making it easier to distinguish between Guest, Normal, and Admin users. Scaffolded the UserController class to handle user actions like registration, login, and profile updates.
Database Connection: Created a basic Database.java class for handling connections using SQLite for now. This will make it easy to switch to PostgreSQL later if needed.
Form Design: Built the initial Register.fxml form with username, password, email, and phone number fields. Added a basic controller method (handleRegister()) for registration logic. Tools We Are Using
JavaFX: Used for creating the graphical user interface (GUI) of our system. FXML files for defining layouts and interactions.
BootstrapFX: We are using this for modern UI styling based on Bootstrap, so you don’t need to write complex CSS.
ControlsFX: This is included for additional UI components like notifications and dialogs, which we may need for admin features.
FormsFX: FormsFX helps simplify form creation and validation, which will be useful for user registration and profile updates.
Database: SQLite is currently set up for local development, but we are aiming to switch to PostgreSQL for better scalability and more complex queries.


Project Structure (Click on raw to display properly)

src/
└── main/
├── javafx/
│    ├── model/
│    │    ├── User.java (Normal User)
│    │    ├── AdminUser.java (extends User)
│    │    ├── GuestUser.java (extends User) // Might change later to have User extend Guest?
│    │    └── Scroll.java (represents a digital scroll)
│    ├── controller/
│    │    ├── UserController.java (handles user-related logic)
│    │    └── ScrollController.java (scaffolded, handles scroll actions)
│    └── view/
│         └── Register.fxml (basic user registration form)
├── resources/
│    └── views/ (all FXML files go here)
└── database/
└── Database.java (manages DB connection)

What Needs to Be Done

User Features: Login: Implement the login functionality in the UserController. Profile Updates: Add methods to allow users to update their profile information.
Scroll Management: Scroll Upload/Download: Implement methods in ScrollController for uploading, downloading, and deleting scrolls. Search and Filters: Implement search filters for scrolls based on uploader ID, scroll ID, name, and upload date.
Admin Features: Admin users should be able to view all users, delete users, and view system statistics (e.g., number of downloads/uploads).
Database Integration: We need to finalize how we will manage users and scroll data in the database. PostgreSQL setup and migration from SQLite should be considered early in the development.
Next Steps for the Team Complete UserController: Someone needs to focus on finishing the login functionality and profile updates. Build ScrollController: Another team member can start working on the scroll upload, search, and management functionality. Admin Dashboard: Create the FXML file and controller for the admin dashboard to manage users and system stats. Test the SQLite Setup: Test the current SQLite database integration for storing user and scroll data, and then work on the transition to PostgreSQL.

CHATGPT explanation of FXML

What are FXML Files and How They Relate to Our JavaFX Project? FXML files in JavaFX are used to define the user interface (UI) of our application in a simple, readable XML format. Instead of writing complex Java code to create buttons, text fields, and layouts, we can describe the UI structure declaratively using FXML. This separates the UI design from the core logic of the application, making it easier to maintain and update.

In our Virtual Scroll Access System (VSAS), we will use FXML to design screens like the registration form, scroll management screen, and admin dashboard. These FXML files will define where buttons, text fields, and other components are placed, while the logic behind what these components do will be handled in the controller classes.

Why Separate Controllers, Views, and Models? In our project, we are following a Model-View-Controller (MVC) design pattern. This approach separates the code into three clear parts:

Model: This is where we define the data and core functionality. For example, our User and Scroll classes represent the data the application handles. These classes don’t interact with the UI directly but store information like user credentials or scroll metadata.

View: This is where the UI layout lives, which in our case is handled by FXML files. These files only describe what the user interface looks like (e.g., what buttons and text fields are on the screen) without any logic for how they work. It keeps the interface separate from the business logic.

Controller: This is where the logic lives. When a user clicks a button or submits a form, the controller takes action. The controller listens to events from the UI and interacts with the model to process data or update the view. For example, if a user clicks the register button in the FXML file, the controller will handle the registration logic, such as checking if the fields are filled out and saving the user data.

Why Is This Separation Helpful? Easier Collaboration: By separating the UI from the logic, different people can work on different parts of the project without stepping on each other’s toes. One person can focus on designing the UI (in FXML) while another works on writing the logic in the controller.

Maintainability: If we need to change how the UI looks (e.g., moving a button or adding a new text field), we only need to update the FXML file. We won’t have to touch the Java code that handles the logic. Similarly, if we change the business logic, we can update the controller without needing to worry about the layout.

Reusable Components: Since the UI is defined separately in FXML, we can easily reuse parts of it across the project. For example, if we need a user input form for both the registration page and the edit profile page, we can define the form once in an FXML file and use it in multiple places.

Clean Code: Separating the view (UI) from the logic keeps our code clean and readable. It’s easier to find bugs and make changes since everything is organized by its purpose.

How FXML Helps Us Simplifies UI Design: Instead of writing long and complex Java code to create buttons, labels, and forms, we describe the layout in a simple XML format (FXML). Visual Tools: We can use JavaFX Scene Builder, a drag-and-drop tool, to design the UI visually, which will make it easier for team members who don’t have experience with JavaFX. Modular Development: FXML makes it easy to develop different parts of the interface independently and reuse them across the project. Example for Better Understanding Let’s say we are working on the registration form. We will define the layout (fields for username, password, email, etc.) in an FXML file, and the logic for what happens when the user clicks Register (such as validating the input and saving the user) will be in the UserController class. This way, we can change the design (like adding a new input field) without touching the registration logic.

In summary, FXML allows us to separate the UI from the logic in our JavaFX project, following the Model-View-Controller (MVC) pattern. This makes the project easier to manage, maintain, and collaborate on, especially since different team members can focus on different parts of the application without interfering with each other’s work.
