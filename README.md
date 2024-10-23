# VideoSuper

VideoSuper is a demo showcasing the WebRTC video feature. It allows peer-to-peer video communication using WebRTC, with Firebase serving as the signaling server.

## Features

- Real-time video communication using WebRTC.
- Firebase Realtime Database is used for signaling and establishing WebRTC connections.

## Prerequisites

Before running the project, ensure that you:

- **Create a project in Firebase**:
  - Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
  - Add an Android app to your Firebase project.
  - Download the `google-services.json` file and place it in your project's `app/` directory.
  
- **Enable Realtime Database**:
  - In the Firebase Console, navigate to **Realtime Database**.
  - Click **Create Database** and follow the prompts to enable it.
  - Set the database rules to allow read/write access during testing:

    ```json
    {
      "rules": {
        ".read": "true",
        ".write": "true"
      }
    }
    ```

    > **Note**: For production, make sure to secure your database rules appropriately.

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/VideoSuper.git
cd VideoSuper
