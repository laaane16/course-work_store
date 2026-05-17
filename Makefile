# Путь к JavaFX SDK
JAVAFX_LIB = ~/javafx-sdk-21.0.11/lib

# Имя главного класса
MAIN_CLASS = Main

# Компиляция
compile:
	javac --module-path $(JAVAFX_LIB) --add-modules javafx.controls $(MAIN_CLASS).java

# Запуск
run:
	java --module-path $(JAVAFX_LIB) --add-modules javafx.controls $(MAIN_CLASS)

# Компиляция + запуск
all: compile run

# Очистка *.class
clean:
	rm -f *.class