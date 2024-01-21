# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v20.4.0-1.20.4] - 2024-01-21
- Ported to Minecraft 1.20.4
- Ported to NeoForge
### Changed
- The vanilla enchanting table block is no longer modified, instead a custom block is added to avoid having to rely on Mixins and running into issues with other mods
- In-game there should be no noticeable difference as the vanilla block is replaced wherever possible
- Add a config option so that re-rolls will cost enchantment levels instead of experience points
