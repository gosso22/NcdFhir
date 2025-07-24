# NCD FHIR Healthcare Application

A comprehensive Android healthcare application designed for Non-Communicable Disease (NCD) screening, particularly for diabetes and hypertension. Built with modern Android development practices and FHIR R4 standards for healthcare data interoperability.

## Overview

The NCD FHIR Healthcare Application empowers healthcare workers with tools to manage patient records, conduct standardized screenings, and synchronize data with healthcare systems. The application works both online and offline, ensuring continuous patient care regardless of connectivity.

## Key Features

### Patient Management
- **Patient List & Search**: Browse and search through patient records with real-time filtering
- **Patient Profiles**: Comprehensive patient information display with demographic details
- **Pagination Support**: Efficient loading of large patient datasets

### NCD Screening
- **FHIR-Compliant Questionnaires**: Standardized screening questionnaires for diabetes and hypertension
- **Interactive Forms**: User-friendly questionnaire interface with validation
- **Location Data Collection**: GPS coordinate capture for screening location tracking
- **Offline Screening**: Complete screenings without internet connectivity

### Data Synchronization
- **Automatic Sync**: Background synchronization with FHIR-compliant healthcare servers
- **Offline Support**: Local data storage with sync when connectivity is restored
- **Real-time Status**: Sync progress indicators and status notifications

### Security & Compliance
- **FHIR R4 Standards**: Full compliance with healthcare interoperability standards
- **Data Encryption**: Local data encryption when device supports it
- **Secure Transmission**: HTTPS-only communication with healthcare servers
- **Privacy Protection**: Healthcare-grade data privacy and security measures

## Technical Architecture

### Technology Stack
- **Language**: Kotlin with Java 11 compatibility
- **UI Framework**: Android Views + Jetpack Compose (hybrid)
- **Architecture**: MVVM with LiveData and Coroutines
- **Healthcare Standards**: FHIR R4 via Google Android FHIR SDK
- **Local Storage**: SQLite with FHIR Engine, DataStore for preferences
- **Navigation**: Android Navigation Component with Safe Args
- **Background Processing**: WorkManager for sync operations

### Key Dependencies
- **Google Android FHIR SDK**: Healthcare data management and FHIR compliance
- **Jetpack Compose**: Modern UI toolkit for patient profiles
- **Navigation Component**: Type-safe navigation between screens
- **WorkManager**: Reliable background data synchronization
- **DataStore**: Modern preferences and settings storage
- **Coroutines**: Asynchronous programming and data operations

## Project Structure

```
app/src/main/java/com/healthtracker/ncdcare/
├── application/           # Application setup and configuration
├── ui/
│   ├── home/             # Patient list and profile screens
│   ├── screening/        # NCD screening questionnaires
│   └── theme/            # UI theming and styling
├── utils/                # Utility functions and helpers
└── view/                 # Custom UI components and widgets
```

## Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 26+ (Android 8.0)
- Java 11 or later
- Gradle 8.9+

### Setup Instructions

1. **Clone the Repository**
   ```bash
   git clone <repository-url>
   cd ncd-fhir-healthcare-app
   ```

2. **Configure FHIR Server**
   Update the server configuration in `NcdFhirApplication.kt`:
   ```kotlin
   ServerConfiguration(
       baseUrl = "https://your-fhir-server.com/fhir/",
       // ... other configuration
   )
   ```

3. **Build and Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### Configuration

#### FHIR Server Setup
The application requires a FHIR R4-compliant server. Configure the base URL in the application class:

```kotlin
FhirEngineProvider.init(
    FhirEngineConfiguration(
        enableEncryptionIfSupported = true,
        DatabaseErrorStrategy.RECREATE_AT_OPEN,
        ServerConfiguration(
            baseUrl = "https://your-fhir-server.com/fhir/",
            httpLogger = HttpLogger(...)
        )
    )
)
```

#### Questionnaire Assets
Place your FHIR questionnaire JSON files in `app/src/main/assets/`:
- `screening-questionnaire.json`: Main NCD screening questionnaire
- `screening-ncd.map`: Questionnaire mapping configuration

## Usage

### For Healthcare Workers

1. **Patient Selection**: Browse or search for patients in the main list
2. **Patient Review**: View patient demographics and medical identifiers
3. **Screening Initiation**: Tap "Screening" to begin NCD assessment
4. **Questionnaire Completion**: Fill out the standardized screening form
5. **Data Submission**: Submit completed screenings for sync with healthcare systems

### For Administrators

1. **Server Configuration**: Set up FHIR server endpoints and authentication
2. **Questionnaire Management**: Update screening questionnaires via assets
3. **Sync Monitoring**: Monitor data synchronization status and logs
4. **User Management**: Configure user access and permissions

## Development

### Building
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```

### Testing
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

### Code Quality
- **ProGuard**: Code obfuscation and optimization for release builds
- **Lint**: Static code analysis with Android Lint
- **KtLint**: Kotlin code style enforcement
- **Detekt**: Static code analysis for Kotlin

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow MVVM architectural patterns
- Write comprehensive unit tests for business logic
- Use Compose for new UI components
- Ensure FHIR R4 compliance for all healthcare data
- Implement proper error handling and offline support

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For technical support or questions:
- Create an issue in the GitHub repository
- Contact the development team
- Review the FHIR R4 documentation for healthcare standards

## Acknowledgments

- Google Android FHIR SDK team for healthcare interoperability tools
- FHIR community for healthcare data standards
- Android development community for modern mobile development practices
