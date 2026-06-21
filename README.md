# NOTELY

> Voice-first Android note-taking application powered by native speech recognition.  
![Android](https://img.shields.io/badge/Platform-Android-green)
![Java](https://img.shields.io/badge/Java-11-orange)
![SDK](https://img.shields.io/badge/SDK-34-blue)
![MinSDK](https://img.shields.io/badge/MinSDK-24-lightgrey)
![License](https://img.shields.io/badge/Status-Portfolio_Project-success)

---

## Overview

NOTELY is a native Android application that enables users to create, manage, and share notes using voice input instead of manual typing.

The application leverages Android's built-in Speech Recognition APIs to convert spoken language into text, allowing users to quickly capture ideas, reminders, meeting notes, and other information through a voice-first workflow.

Designed as a lightweight productivity tool, NOTELY demonstrates practical Android development concepts including speech recognition, local persistence, runtime permission management, Material Design components, and intent-based application interoperability.

---

## Features

### Voice-to-Text Note Creation

* Convert spoken input into text using Android SpeechRecognizer APIs
* Support for multiple recording sessions within a single note
* Real-time transcription workflow

### Note Management

* Create notes with custom titles
* View saved notes
* Edit existing notes
* Delete notes
* Maintain recent note history

### Local Persistence

* Automatic note saving
* Persistent storage using SharedPreferences
* JSON serialization using Gson
* No external database required

### Sharing & Productivity

* Share notes through Android Share Intents
* Compatible with WhatsApp, email clients, messaging applications, and other supported apps
* Copy note content directly to clipboard

### User Experience

* Material Design components
* Navigation drawer interface
* Floating Action Button (FAB) interactions
* Runtime microphone permission handling

---

## Architecture

NOTELY follows a lightweight event-driven Android architecture built around native Android components.

```text
                    User
                      │
                      ▼
            Speech Recognition
                      │
                      ▼
             Text Processing
                      │
                      ▼
              Local Persistence
         (SharedPreferences + Gson)
                      │
                      ▼
              Note Management
                      │
      ┌────────┬──────┼──────┬────────┐
      │        │      │      │        │
      ▼        ▼      ▼      ▼        ▼
    Create   Edit   Share   Copy   Delete

```

---

## Technology Stack

### Mobile Development

* Java 11
* Android SDK 34
* Android Studio
* XML Layouts

### Android APIs

* SpeechRecognizer
* RecognizerIntent
* SharedPreferences
* ClipboardManager
* Intent-Based Sharing

### Libraries

* Material Components
* Gson

---

## Project Structure

```text
NOTELY
│
├── app
│   ├── manifests
│   ├── java
│   │   └── com.example.notely
│   │       └── MainActivity.java
│   │
│   ├── res
│   │   ├── layout
│   │   ├── menu
│   │   ├── drawable
│   │   └── values
│   │
│   └── AndroidManifest.xml
│
├── Gradle Scripts
└── README.md
```

---

## Application Workflow

### Creating a Note

1. Create a new note
2. Provide a note title
3. Start voice recording
4. Speak naturally
5. Speech is converted into text
6. Save the note
7. Access the note from the recent notes list

### Managing Notes

Users can:

* Edit note content
* Delete notes
* Copy content to clipboard
* Share notes to external applications

---

## Storage Design

NOTELY uses a lightweight local persistence strategy.

```text
Notes
   │
   ▼
Gson Serialization
   │
   ▼
JSON Storage
   │
   ▼
SharedPreferences
```

### Why This Approach?

* Simple implementation
* Lightweight storage footprint
* No SQLite setup required
* Fast read/write operations
* Suitable for prototype and productivity applications

---

## Permissions

The application requires microphone access for speech recognition.

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
```

---

## Installation

### Prerequisites

* Android Studio
* Android SDK 34
* Java 11

### Clone Repository

```bash
git clone https://github.com/<username>/NOTELY.git
```

### Run Application

1. Open project in Android Studio
2. Sync Gradle dependencies
3. Connect an Android device or launch an emulator
4. Build and run the application

---

## Technical Highlights

This project demonstrates practical experience with:

* Native Android application development
* Speech recognition integration
* Runtime permission handling
* Local data persistence
* JSON serialization
* Material Design implementation
* Android Intent framework
* Event-driven mobile application workflows

---

## Engineering Challenges Solved

### Speech Recognition Lifecycle Management

Handled initialization, recording sessions, result processing, and error recovery using Android's RecognitionListener interface.

### Persistent Note Storage

Implemented local note persistence using SharedPreferences and Gson serialization to maintain note history across application restarts.

### Application Interoperability

Enabled seamless sharing across third-party applications through Android's native Intent system.

### State Management

Managed note creation, editing, viewing, and persistence workflows within a unified Android activity architecture.

---

## Roadmap

### Completed

* [x] Speech-to-text transcription
* [x] Local note persistence
* [x] Note editing
* [x] Note deletion
* [x] Clipboard integration
* [x] Cross-application sharing

### Planned

* [ ] PDF export support
* [ ] DOCX document support
* [ ] Advanced note organization
* [ ] Search and filtering
* [ ] Cloud synchronization
* [ ] Offline speech processing
* [ ] AI-powered note summarization
* [ ] Rich text editing

---

## Motivation

Traditional note-taking applications rely heavily on manual text entry. NOTELY explores a voice-first interaction model where spoken language becomes the primary method for capturing and organizing information.

The project was built to explore Android speech recognition technologies and improve productivity workflows through natural voice interaction.

---

## Author

**Jaganathan J**

Software Engineer • Backend Developer • Distributed Systems Engineer

---

⭐ If you found this project interesting, consider starring the repository.
