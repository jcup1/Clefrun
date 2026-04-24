.PHONY: tests unit ui build clean install run

tests:
	./scripts/run-tests.sh

unit:
	./gradlew test

ui:
	./gradlew connectedAndroidTest

build:
	./gradlew build

clean:
	./gradlew clean

install:
	./gradlew installDebug

run:
	./gradlew installDebug
	adb shell monkey -p com.clefrun.app -c android.intent.category.LAUNCHER 1