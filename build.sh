# This script builds and packages the game for distribution.

# Build game jar
gradle DungeoneerDesktop:processResources
gradle DungeoneerDesktop:dist

# Build non-steam game jar
gradle DungeoneerDesktopNoSteam:processResources
gradle DungeoneerDesktopNoSteam:dist

# Build editor jar
gradle DelvEdit:processResources
gradle DelvEdit:dist
