package network;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

public class JSONParser {

    private static final LZ77Compressor compressor = new LZ77Compressor();

    /**
     * Construye un mensaje JSON listo para enviar.
     * @param username nombre del usuario
     * @param message  texto del mensaje (sin comprimir)
     * @param ip       dirección IP del emisor
     * @return String con formato {"username":"...","message":"<base64>","ip":"..."}
     */
    public static String createMessage(String username, String message, String ip) {
        String compressedBase64 = compressor.compress(message);
        return String.format("{\"username\":\"%s\",\"message\":\"%s\",\"ip\":\"%s\"}",
                escapeJson(username),
                compressedBase64,
                escapeJson(ip));
    }

    /**
     * Analiza un mensaje JSON y devuelve un objeto Message con los datos descomprimidos.
     * @param json cadena JSON recibida
     * @return objeto Message o null si hay error
     */
    public static Message parseMessage(String json) {
        try {
            String username = extractField(json, "username");
            String compressedMsg = extractField(json, "message");
            String ip = extractField(json, "ip");

            if (username == null || compressedMsg == null || ip == null)
                throw new IllegalArgumentException("Faltan campos en JSON");

            String decompressed = compressor.decompress(compressedMsg);
            return new Message(username, decompressed, ip);
        } catch (Exception e) {
            System.err.println("[JSONParser] Error parseando: " + e.getMessage());
            return null;
        }
    }

    /**
     * Envía un mensaje (ya construido con createMessage) a través del socket.
     */
    public static void sendMessage(Socket socket, String jsonMessage) throws Exception {
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        out.println(jsonMessage);
    }

    // -------------------------------------------------------------
    // Método auxiliar para extraer campos de un JSON simple
    // (compatible con el usado en MessageListener)
    // -------------------------------------------------------------
    private static String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return null;

        int valueStart = json.indexOf('"', keyIndex + key.length() + 1);
        if (valueStart == -1) return null;

        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd == -1) return null;

        return json.substring(valueStart + 1, valueEnd);
    }

    // Escape básico para evitar romper el JSON
    private static String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    // -------------------------------------------------------------
    // Clase interna para almacenar el mensaje descomprimido
    // (similar a IncomingMessage pero independiente)
    // -------------------------------------------------------------
    public static class Message {
        public final String username;
        public final String message;
        public final String ip;

        public Message(String username, String message, String ip) {
            this.username = username;
            this.message = message;
            this.ip = ip;
        }

        @Override
        public String toString() {
            return "[" + ip + "] " + username + ": " + message;
        }
    }
}