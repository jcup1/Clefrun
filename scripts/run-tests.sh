#!/bin/bash

echo "Running unit tests..."
./gradlew test

echo "Running instrumented tests..."
./gradlew connectedAndroidTest

echo "All tests finished!"