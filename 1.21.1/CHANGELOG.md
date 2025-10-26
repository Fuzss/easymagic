# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v21.1.4-1.21.1] - 2025-10-26

### Fixed

- Fix enchanting slot tooltips rendering twice

## [v21.1.3-1.21.1] - 2025-10-24

### Changed

- This update backports the enchanting table menu and screen from modern versions of Easy Magic
- This includes reliable enchantment hints on the client and improved item quick moving (via shift-clicking) for the
  menu
- The screen now uses actual buttons which allow for properly placed tooltips, and cycling between them using the `Tab`
  key, etc.
- Also, tooltips are no longer computed on every frame for a small performance boost

## [v21.1.2-1.21.1] - 2025-10-17

### Changed

- Include enchantment descriptions in enchanting table hint tooltips if available

## [v21.1.1-1.21.1] - 2024-10-05

### Fixed

- Fix enchantment hints failing to sync to some clients on multiplayer servers

## [v21.1.0-1.21.1] - 2024-09-18

### Changed

- Port to Minecraft 1.21.1
