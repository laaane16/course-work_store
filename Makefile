# Путь к JavaFX SDK
JAVAFX_LIB = $(HOME)/javafx-sdk-21.0.11/lib
WIN_JAVAFX_LIB ?= C:/javafx-sdk-21.0.11/lib

# Имя главного класса
MAIN_CLASS = Main
APP_NAME = Store
DIST_DIR = dist
INPUT_DIR = $(DIST_DIR)/input
WIN_INPUT_DIR = $(DIST_DIR)/win-input
WIN_JPACKAGE_TYPE ?= exe

# Компиляция
compile:
	javac --module-path $(JAVAFX_LIB) --add-modules javafx.controls $(MAIN_CLASS).java

# Исполняемый JAR
jar: compile
	mkdir -p $(DIST_DIR)
	jar --create --file $(DIST_DIR)/$(APP_NAME).jar --main-class $(MAIN_CLASS) *.class

# Запуск собранного JAR
run-jar: jar
	java --module-path $(JAVAFX_LIB) --add-modules javafx.controls -jar $(DIST_DIR)/$(APP_NAME).jar

# Приложение macOS через jpackage
package-mac: jar
	mkdir -p $(INPUT_DIR)
	cp $(DIST_DIR)/$(APP_NAME).jar $(INPUT_DIR)/
	jpackage \
		--type app-image \
		--name $(APP_NAME) \
		--input $(INPUT_DIR) \
		--main-jar $(APP_NAME).jar \
		--main-class $(MAIN_CLASS) \
		--java-options "--module-path $(JAVAFX_LIB)" \
		--java-options "--add-modules javafx.controls"

# Сборка Windows-пакета через jpackage.
# Эту цель нужно запускать на Windows, где установлен JDK с jpackage и JavaFX SDK.
package-win: jar
	mkdir -p $(WIN_INPUT_DIR)
	cp $(DIST_DIR)/$(APP_NAME).jar $(WIN_INPUT_DIR)/
	jpackage \
		--type $(WIN_JPACKAGE_TYPE) \
		--dest $(DIST_DIR) \
		--name $(APP_NAME) \
		--input $(WIN_INPUT_DIR) \
		--main-jar $(APP_NAME).jar \
		--main-class $(MAIN_CLASS) \
		--java-options "--module-path $(WIN_JAVAFX_LIB)" \
		--java-options "--add-modules javafx.controls"

# Запуск
run:
	java --module-path $(JAVAFX_LIB) --add-modules javafx.controls $(MAIN_CLASS)

# Компиляция + запуск
all: compile run

# Очистка *.class
clean:
	rm -f *.class
	rm -rf $(DIST_DIR) $(APP_NAME).app
