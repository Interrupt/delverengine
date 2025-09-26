#!/bin/sh

# This script builds and packages for distribution.

# Build game jar
./gradlew DungeoneerDesktop:processResources
./gradlew DungeoneerDesktop:dist

# Build editor jar
./gradlew DelvEdit:processResources
./gradlew DelvEdit:dist
