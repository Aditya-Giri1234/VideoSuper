# VideoSuper - Real-Time WebRTC Video Chat

**VideoSuper** is a demo Android app that showcases peer-to-peer video communication using WebRTC, with Firebase Realtime Database used for signaling. This app allows users to connect through video calls, providing a simple yet functional demonstration of real-time communication on mobile devices.

## Features

- **Peer-to-Peer Video Communication**: Establish secure video calls between users using WebRTC.
- **Firebase Realtime Database**: Firebase is used for signaling and managing connections between peers.
- **User Authentication**: Firebase Authentication to securely handle user sign-ins.
- **Incoming Call Notifications**: Alerts users of incoming calls with ringtones in the app.
- **Permissions Management**: Requests necessary permissions (camera, microphone, etc.) via PermissionX.
- **Minimalist UI**: A clean, simple UI for seamless video communication.

## Prerequisites

Before running the project, follow these steps to configure the Firebase project and dependencies.

### 1. Firebase Project Setup

- **Create a Firebase Project**:
  - Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
  - Add an Android app to your Firebase project.
  - Download the `google-services.json` file and place it in the `app/` directory of your project.

- **Enable Firebase Realtime Database**:
  - In the Firebase Console, navigate to **Realtime Database** and click on **Create Database**.
  - Set the rules for development to allow read and write access:
  
    ```json
    {
      "rules": {
        ".read": "true",
        ".write": "true"
      }
    }
    ```
    
    **Important**: Update these rules for production by securing your data access appropriately.

- **Enable Firebase Authentication**:
  - In the Firebase Console, enable **Email/Password Authentication** or choose other authentication methods if required for your app.

### 2. WebRTC Setup

This project uses WebRTC for establishing video communication. Ensure that WebRTC dependencies are correctly included in your `build.gradle` files.

### 3. Clone the Repository

To get started, clone this repository to your local machine:

```bash
git clone https://github.com/your-username/VideoSuper.git
cd VideoSuper
