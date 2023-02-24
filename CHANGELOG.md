# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

## [v4.3.3-1.19.2] - 2023-02-24
### Added
- Added an option to make reroll catalysts separate from enchanting catalysts
  - This means rerolling no longer uses lapis lazuli, but a different dedicated item instead
  - When the option is enabled, a new slot is added to enchanting tables for the reroll item
- Enchanting catalyst items are now defined by the `easymagic:enchanting_catalysts` item tag
- Reroll catalyst items are now defined by the `easymagic:reroll_catalysts` item tag
### Changed
- The lenient bookshelves option now makes enchanting tables ignore all blocks that do not have a full block collision shape (like carpet) when counting bookshelves, before this would only ignore blocks without a collision shape (like torches)

## [v4.3.2-1.19.2] - 2022-08-27
### Fixed
- Reroll button now behaves as intended in creative mode

## [v4.3.1-1.19.2] - 2022-08-27
### Fixed
- Minor fixes for the reroll button showing when it wasn't actually usable

## [v4.3.0-1.19.2] - 2022-08-21
- Compiled for Minecraft 1.19.2

## [v4.2.0-1.19.1] - 2022-08-15
### Changed
- Rerolling is now done via a dedicated button which replaces the book in the enchanting screen (there's a client config option to still render the book with the same functionality when clicked)
- Rerolling now costs experience points instead of enchantment levels to not ruin your level progress for enchanting as much 
- Items in an enchanting table now always disappear from rendering when the player moves too far away (lapis lazuli still stays floating around the table)
### Removed
- Removed fancy rendering option, fancy floating does the same thing basically
- Removed config option to restrict the enchanting table input slot to only enchantable items

## [v4.1.0-1.19.1] - 2022-07-30
- Compiled for Minecraft 1.19.1
- Updated to Puzzles Lib v4.1.0

## [v4.0.0-1.19] - 2022-07-17
- Ported to Minecraft 1.19
- Split into multi-loader project
### Changed
- Enchanting table contents will no longer render when the player is too far away (can be changed back in the client config)
- Showing all enchantments you'll receive no longer adds a question mark
### Fixed
- Re-rolling is no longer possible without an item in the enchanting slot
- Modded bookshelves are now recognized on Fabric

[Keep a Changelog]: https://keepachangelog.com/en/1.0.0/
