# Extended Contacts

<p align="center">
  <img src="https://i.imgur.com/9td2pjM.png"/>
</p>

**Extended Contacts** is a modern, feature-rich Android application designed to manage your personal and professional contacts with ease. Built with **Material Design 3** and the latest Android development practices, it offers a clean, intuitive, and highly functional experience.

## Features

- **Advanced Contact Management**: Full CRUD (Create, Read, Update, Delete) operations for your contacts.
- **Organization by Groups**: Categorize your contacts into custom groups like "Friends", "Work", "Family", etc.
- **Favorites**: Star your most important contacts for quick access in a dedicated section.
- **Smart Search**: Find anyone instantly using the real-time search bar.
- **Photo Editor Integration**: Set contact photos using your camera or gallery with built-in cropping and scaling powered by **uCrop**.
- **Import/Export (VCF)**: Seamlessly migrate your contacts using standard `.vcf` (vCard) files.
- **Material 3 UI**: Beautiful interface with support for **Dark Mode** and dynamic color synchronization.
- **Adaptive Icon**: A professionally designed adaptive logo that looks great on any launcher.

## Tech Stack

- **Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Dependency Injection**: [Dagger Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Image Loading**: [Glide](https://github.com/bumptech/glide)
- **Image Cropping**: [uCrop](https://github.com/yalantis/uCrop)
- **UI Components**: Material Design 3 (M3)

## Architecture

The app follows the official Google recommendation for app architecture:
- **UI Layer**: Activities and Fragments using ViewBinding.
- **ViewModel**: Manages UI-related data and handles communication between UI and Repository.
- **Repository**: A clean API for data access, managing the Room database.
- **Data Source**: Room SQLite database for local persistence.

## Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Z1vvs/ExtendedContacts.git
   ```
2. **Open the project** in Android Studio (Ladybug or newer recommended).
3. **Build and Run** the app on an emulator or a physical device (API 24+).

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.
