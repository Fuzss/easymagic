# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog].

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
