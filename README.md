# Face Detection Android App

A simple Android application that demonstrates real-time face detection using the device's camera. The app uses CameraX for camera management and ML Kit's Face Detection API to identify faces in the camera preview and draw a bounding box around them.

## Features

*   Real-time camera preview using Jetpack Compose.
*   Face detection using Google's ML Kit.
*   Drawing bounding boxes around detected faces.
*   Handles camera permission requests.

## Built With

*   [Kotlin](https://kotlinlang.org/) - First-class and official programming language for Android development.
*   [Jetpack Compose](https://developer.android.com/jetpack/compose) - Android’s modern toolkit for building native UI.
*   [CameraX](https://developer.android.com/training/camerax) - A Jetpack support library, built to help you make camera app development easier.
*   [ML Kit Face Detection](https://developers.google.com/ml-kit/vision/face-detection) - A Google API that can detect faces in an image or video.
*   [Accompanist Permissions](https://google.github.io/accompanist/permissions/) - A library that provides easy-to-use composables and utilities for working with Android runtime permissions.

## Getting Started

To get a local copy up and running follow these simple steps.

### Prerequisites

*   Android Studio Iguana | 2023.2.1 or later.
*   Android SDK 34 or later.
*   An Android device or emulator with a camera.

### Installation

1.  Clone the repo
    ```sh
    git clone https://github.com/hi-mod/face-detection.git
    ```
2.  Open the project in Android Studio.
3.  Build the project and run it on an Android device or emulator.

## Usage

1.  Launch the app.
2.  Grant camera permission when prompted.
3.  Point the camera at a face.
4.  A green rectangle will be drawn around the detected face(s).
