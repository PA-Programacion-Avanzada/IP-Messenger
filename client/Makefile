# Makefile para IP Messenger Client
# Compila todos los ficheros Java y ejecuta Main

# ── Directorios ------------------------------------------------------------
SRC_DIR := IPMessengerClient/src
BIN_DIR := out
LIB_DIR := ../lib

# ── Archivos --------------------------------------------------------------
SRCS := $(shell find $(SRC_DIR) -name "*.java")

# ── JARs ---------------------------------------------------------------
GSON_JAR := $(LIB_DIR)/gson-2.11.0.jar

CP_JARS := $(GSON_JAR)

# ── Meta‑objetivos --------------------------------------------------------
.PHONY: all compile run clean

all: compile run

# ── Compilación -----------------------------------------------------------
compile:
	@echo "Compilando fuentes ..."
	mkdir -p $(BIN_DIR)
	@#‑cp  incluye las dependencias
	javac -cp "$(CP_JARS)" -d $(BIN_DIR) $(SRCS)

# ── Ejecución ------------------------------------------------------------
run:
	@echo "Ejecución ..."
	@#‑cp  contiene el binario y los JARs
	java -cp "$(BIN_DIR):$(CP_JARS)" com.ipmessenger.client.Main

# ── Limpieza --------------------------------------------------------------
clean:
	@echo "Limpieza ..."
	rm -rf $(BIN_DIR)