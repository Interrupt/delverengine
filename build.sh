# This script builds and packages for distribution.

# Build game jar
gradle DungeoneerDesktop:processResources
gradle DungeoneerDesktop:dist

# Build editor jar
gradle DelvEdit:processResources
gradle DelvEdit:dist
