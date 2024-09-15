<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# checkov-jetbrains-idea Changelog

## [1.0.22] - 2024-09-15

### Added

- Added a new 'show logs' button in the plugin panel that opens the plugin log file
- Added validations to user input in the configuration screen 
- Added a `test connection` button in the configuration screen

### Misc

- Upgraded gradle version to 8.10.1
- Migrated the IntelliJ platform plugin to version 2.0.1
- Refactored `ApiClient` (now `PrismaApiClient`) and `AnalyticsService` (*Work in progress*)

## [1.0.21] - 2024-08-29

### Added

- Added the following data to Prisma Cloud analytics

  - Extension version
  - VS Code version
  - Operating system
  - Checkov version

### Fixed

- Fixed installation issues with pip when using externally managed python installation
- Stabilized build time of the application and added support for multiple IDE versions
- Fixed various crashes

## [0.0.3-beta]
### Added

* Updated platform build range
*

## [0.0.1]
### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
