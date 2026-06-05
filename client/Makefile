# Makefile for IP Messenger Client
# Compile all Java sources and run Main

# Directories
SRC_DIR := IPMessengerClient/src
BIN_DIR := out

# Find all .java files in SRC_DIR
SRCS := $(shell find $(SRC_DIR) -name "*.java")

# Default target
all: compile run

# Compile sources
compile:
	@echo "Compiling sources..."
	mkdir -p $(BIN_DIR)
	javac -d $(BIN_DIR) $(SRCS)

# Run the application
run:
	@echo "Running application..."
	java -cp $(BIN_DIR) Main

# Clean intermediate files
clean:
	@echo "Cleaning..."
	rm -rf $(BIN_DIR)

.PHONY: all compile run clean
