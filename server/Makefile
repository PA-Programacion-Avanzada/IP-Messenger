# Makefile para IP Messenger Server
# Compila todos los ficheros Java y ejecuta Main

SRC_DIR  := IPMessengerServer/src
BIN_DIR  := out
LIB_DIR  := ../lib

# Encuentra TODOS los .java dentro de src/
SRCS := $(shell find $(SRC_DIR) -name "*.java")

# Dependencias que necesita el servidor
GSON_JAR  := $(LIB_DIR)/gson-2.11.0.jar
SQLITE_JAR := $(LIB_DIR)/sqlite-jdbc-3.47.2.0.jar
CP_JARS  := $(GSON_JAR):$(SQLITE_JAR)

.PHONY: all compile run clean

all: compile run

# ---------- Compilación ----------------------------------------
compile:
	mkdir -p $(BIN_DIR)
	javac -cp "$(CP_JARS)" -d $(BIN_DIR) $(SRCS)

# ---------- Lanzamiento ---------------------------------------
run:
	java -cp "$(BIN_DIR):$(CP_JARS)" com.ipmessenger.server.Main

# ---------- Limpieza -------------------------------------------
clean:
	rm -rf $(BIN_DIR)