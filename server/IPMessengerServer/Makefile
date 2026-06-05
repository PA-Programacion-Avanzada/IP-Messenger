# Makefile for IP Messenger Server
# Compile all Java source files under src and run Main

SRC_DIR := src
BIN_DIR := out

SRCS := $(shell find $(SRC_DIR) -name "*.java")

all: compile run

compile:
	@echo "Compiling server sources..."
	mkdir -p $(BIN_DIR)
	javac -d $(BIN_DIR) $(SRCS)

run:
	@echo "Running server..."
	java -cp $(BIN_DIR) Main

clean:
	@echo "Cleaning..."
	rm -rf $(BIN_DIR)

.PHONY: all compile run clean
