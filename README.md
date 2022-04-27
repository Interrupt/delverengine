# [![delverengine](.media/logo.svg?sanitize=true)](https://github.com/interrupt/delverengine)
[![License: Zlib](https://img.shields.io/badge/License-Zlib-lightgrey.svg)](https://opensource.org/licenses/Zlib) [![Discord](https://img.shields.io/discord/266998536632139776.svg?logo=discord&logoColor=white&logoWidth=20&labelColor=7289DA&label=Discord&color=17cf48)](https://discord.gg/gyhmH5f)

# Delver Engine Open Source
Delver engine and editor source code release

This source release does not contain or cover the game data from Delver, the game data remains subject to the original copyright and applicable law.

## Building
To compile on your own ensure you have installed [JDK8](https://adoptium.net/) or higher. Open a terminal to the repo root and run the following commands:

### Game
 
_Running:_ `gradlew DungeoneerDesktop:run`  
_Building:_ `gradlew DungeoneerDesktop:dist`  

### Editor

_Running:_ `gradlew DelvEdit:run`   
_Building:_ `gradlew DelvEdit:dist`  

## License

This source code release is licensed under the zlib Open Source license. [See LICENSE.txt for more information.](LICENSE.txt)

## Notes

Delver is a Java project most easily built via Gradle. Import the Gradle project into your IDE of choice.

This is built on the LibGDX game framework, more information on LibGDX is available at https://libgdx.badlogicgames.com/

For discussion and help, check out the [Official Delver Community Discord](https://discord.gg/gyhmH5f)

### Main Applications

Run configurations for IntelliJ have been included, for manual setup use the following:

Game: `DungeoneerDesktop/src/com/interrupt/dungeoneer/DesktopStarter.java`

Editor: `DelvEdit/src/com/interrupt/dungeoneer/EditorStarter.java`

Working directory: `Dungeoneer`

Resources directory: `Dungeoneer/assets`
