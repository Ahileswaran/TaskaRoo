TaskaRoo

TaskaRoo is an Android mobile application designed to assist users in managing their daily tasks with ease. It integrates unique features like location-based tasks and PDF export, enhancing the traditional to-do list experience.
App Overview

TaskaRoo aims to help users keep track of their day-to-day activities, offering guidance and organization through a user-friendly interface.
Features
1. User Interface

    Main Activity Window: Includes a Floating Action Button (FAB) to add tasks.
    Task Display: Tasks are categorized as overdue, pending, or completed with visual indicators.

2. Task Management

    Add Task: Input task name, description, date, time, and set reminders.
    Edit Task: Double-tap a task to update details or change settings.
    Delete Task: Swipe left or right to prompt a deletion confirmation.

3. Unique Functionalities

    Map Integration: Attach locations to tasks for spatial context.
    Camera Support: Add images to tasks using the device camera.
    PDF Export: Generate a PDF of tasks for sharing or printing.
    Backup/Restore: Securely back up data and restore from previous saves.
    Dark/Light Theme: Toggle between themes for comfortable viewing.

4. Navigation Drawer

    Backup/Restore: Easily access options for data management.
    Theme Toggle: Switch between dark and light themes.
    Export to PDF: Save your tasks as a PDF file.

5. Testing

    Utilizes Espresso framework for UI testing, ensuring functionalities like form resets work as intended.

Installation

## Repository Link

**[TaskaRoo on GitHub](https://github.com/Ahileswaran/TaskaRoo)**


    Open the project in Android Studio.
    Build and run the app on your device or emulator.

How to Use

    Adding a Task:
        Tap the FAB on the main screen.
        Fill in the task details (name, description, date, time).
        Use the map icon to add a location, or the camera icon to include a photo.
        Save the task.

    Editing a Task:
        Double-tap on an existing task.
        Modify any details and save changes.

    Deleting a Task:
        Swipe the task entry to either side.
        Confirm the deletion when prompted.

    Exporting Tasks:
        Access the navigation drawer.
        Select "Export to PDF" and choose a save location.
        Generate and save your PDF.

    Backup and Restore:
        Use the navigation drawer to access backup/restore options.
        Save your current tasks or restore from a previous backup.

Unique Features

    Location-Based Tasks: Assign locations to tasks, visualizing them on a map.
    PDF Export: Easily create a printable version of your task list.
    User-Friendly Interface: Intuitive design with easy navigation and task management.
    Theme Options: Customize the app appearance with light and dark themes.

Testing

TaskaRoo includes automated tests using the Espresso framework to ensure functionality and reliability. Key tests include:

    Reset button functionality within the AddTaskActivity.
    UI responsiveness and permissions handling.

Credits

Developed by Ahileswaran B (E2145015) as part of the Mobile Application Development course at the University of Moratuwa.
