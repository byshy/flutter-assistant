# flutter-assistant

<!-- Plugin description -->
The **Flutter assistant** is a plugin for Android Studio and IntelliJ that helps developers ensure all BLoC events are in use. This tool checks for unused event classes and provides helpful suggestion to ✨automagically✨ generate the handler inside the related BLoC.
<!-- Plugin description end -->

## Features

- **Unused Class Detection**: Automatically detects BLoC event classes that aren't used in the related BLoC and highlights them for review.
- **Fix Suggestions**: Provides actionable suggestions for handling unused events.
- **Code Quality Insights**: Helps maintain clean code by flagging unnecessary classes, improving code readability and reducing project clutter.

## Requirements

- **Android Studio**: This plugin is compatible with Android Studio and IntelliJ versions supporting Flutter and Dart projects.

## Installation

1. Download the latest release of the **Flutter assistant** from the [JetBrains Marketplace](https://plugins.jetbrains.com/).
2. Open Android Studio, go to **File > Settings > Plugins**.
3. Click on the **Gear icon > Install Plugin from Disk** and select the downloaded `.zip` or `.jar` file.
4. Restart Android Studio to activate the plugin.

Alternatively, you can install the plugin directly from the JetBrains Marketplace:

1. Go to **File > Settings > Plugins**.
2. Search for "Flutter assistant" in the marketplace.
3. Click **Install** and restart Android Studio.

## Usage

1. **Analyze Dart Classes**:
  - Once installed, open any BLoC event file in your Flutter project.
  - The plugin will automatically scan your file for unused classes.

2. **View Suggestions**:
  - For any unused events detected, the plugin displays a popup of suggested actions.
  - Click on **Add BLoC handler for unused event** to automatically apply the recommended changes.

## Example

When an unused BLoC event class is detected, the plugin will suggest:

> "Add BLoC handler for unused event."

## Configuration

No additional configuration is required. Once installed, the plugin runs seamlessly in the background.

## Contributing

Contributions are welcome! Feel free to open issues for bugs, suggestions, or improvements.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

> Created by Basel, a mobile team tech lead with expertise in Flutter and clean architecture.
