package network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import network.LZ77Compressor;

public class MessageListener implements Runnable {

    private final Socket socket;
    private volatile boolean running = true;
    private final LZ77Compressor compressor;

    private OnMessageReceivedListener onMessageReceivedListener;
    private OnConnectionLostListener  onConnectionLostListener;

    public static class IncomingMessage {
        public final String username;
        public final String message;   // ya descomprimido
        public final String ip;

        public IncomingMessage(String username, String message, String ip) {
            this.username = username;
            this.message  = message;
            this.ip       = ip;
        }

        @Override
        public String toString() {
            return "[" + ip + "] " + username + ": " + message;
        }
    }

    public interface OnMessageReceivedListener {
        void onMessageReceived(IncomingMessage message);
    }

    public interface OnConnectionLostListener {
        void onConnectionLost(String reason);
    }

    public MessageListener(Socket socket) {
        this.socket     = socket;
        this.compressor = new LZ77Compressor();   // sin parámetros ahora
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"))) {

            String line;
            while (running && (line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                try {
                    IncomingMessage msg = parseAndDecompress(trimmed);
                    if (onMessageReceivedListener != null) {
                        onMessageReceivedListener.onMessageReceived(msg);
                    }
                } catch (Exception e) {
                    System.err.println("[MessageListener] Mensaje malformado: " + e.getMessage());
                }
            }

            if (running && onConnectionLostListener != null) {
                onConnectionLostListener.onConnectionLost("El servidor cerró la conexión");
            }

        } catch (IOException e) {
            if (running && onConnectionLostListener != null) {
                onConnectionLostListener.onConnectionLost("Error de lectura: " + e.getMessage());
            }
        }
    }

    /**
     * Extrae los tres campos del JSON y descomprime el mensaje.
     * JSON esperado: {"username":"...","message":"<base64>","ip":"..."}
     */
    private IncomingMessage parseAndDecompress(String json) throws Exception {
        String username   = extractField(json, "username");
        String compressed = extractField(json, "message");
        String ip         = extractField(json, "ip");

        if (username == null || compressed == null || ip == null) {
            throw new Exception("Faltan campos en: " + json);
        }

        String decompressed = compressor.decompress(compressed);
        return new IncomingMessage(username, decompressed, ip);
    }

    /**
     * Extrae el valor string de un campo JSON simple.
     * Cuando JSONParser esté implementado, reemplazar esta llamada por él.
     */
    private String extractField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) return null;

        int valueStart = json.indexOf('"', keyIndex + key.length() + 1);
        if (valueStart == -1) return null;

        int valueEnd = json.indexOf('"', valueStart + 1);
        if (valueEnd == -1) return null;

        return json.substring(valueStart + 1, valueEnd);
    }

    public void stop() {
        running = false;
        try {
            socket.close();
        } catch (IOException e) {
            // ignorar al cerrar
        }
    }

    public void setOnMessageReceivedListener(OnMessageReceivedListener listener) {
        this.onMessageReceivedListener = listener;
    }

    public void setOnConnectionLostListener(OnConnectionLostListener listener) {
        this.onConnectionLostListener = listener;
    }
}