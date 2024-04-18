# Unsplash Infinite Loader

This Android application provides a seamless experience for browsing and loading images from the Unsplash API with infinite scrolling functionality. It incorporates asynchronous image loading, caching mechanisms for efficient retrieval, and robust error handling for graceful handling of network errors and image loading failures.

## Features

- **Asynchronous Image Loading**: Images are fetched from the Unsplash API asynchronously, ensuring smooth performance and responsiveness.
- **Caching Mechanism**: Implementing both memory and disk caching optimizes image retrieval, reducing load times and bandwidth usage.
- **Infinite Scroll**: Users can endlessly scroll through a continuous feed of images, providing an immersive browsing experience.
- **Error Handling**: Network errors and image loading failures are handled gracefully, with informative error messages or placeholders displayed to the user.

## Getting Started

To run the project locally, follow these steps:

1. **Create an Unsplash Account and Obtain Access Key**: Sign up for an account on Unsplash and create a new application to obtain an access key. Copy the access key.
   
2. **Add Access Key to local.properties**: Open the `local.properties` file in the root directory of the project. Add the following line to the file, replacing `YOUR_UNSPLASH_ACCESS_KEY` with your actual Unsplash access key:

    ```
    ACCESS_KEY=YOUR_UNSPLASH_ACCESS_KEY
    ```

3. **Build and Run**: Open the project in Android Studio and build/run the application on your device or emulator.

## Contributing

Contributions are welcome! If you have any suggestions, bug fixes, or new features to add, feel free to open an issue or submit a pull request.

## Acknowledgments

- Special thanks to Unsplash for providing access to their vast collection of high-quality images through their API.

