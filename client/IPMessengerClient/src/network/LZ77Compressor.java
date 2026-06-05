package network;

import java.util.ArrayList;
import java.util.List;
import java.util.Base64;

public class LZ77Compressor {

    private static final int WINDOW_SIZE = 255;
    private static final int MAX_MATCH   = 255;

    // ── Comprimir ──────────────────────────────────────────────────────────────

    /**
     * Comprime un String y devuelve los bytes comprimidos en Base64
     * para que sea seguro meterlo en JSON.
     */
    public String compress(String text) {
        if (text == null) text = "";
        byte[] input  = text.getBytes();
        byte[] compressed = compressBytes(input);
        return Base64.getEncoder().encodeToString(compressed);
    }

    /**
     * Descomprime un String Base64 (producido por compress()) y devuelve
     * el texto original.
     */
    public String decompress(String base64Compressed) {
        if (base64Compressed == null || base64Compressed.isEmpty()) return "";
        byte[] compressedBytes = Base64.getDecoder().decode(base64Compressed);
        return decompressBytes(compressedBytes);
    }

    // ── Lógica interna ─────────────────────────────────────────────────────────

    private byte[] compressBytes(byte[] originalBytes) {
        List<Byte> output = new ArrayList<>();
        int i = 0;
        int n = originalBytes.length;

        while (i < n) {
            int bestOffset = 0;
            int bestLength = 0;
            int searchStart = Math.max(0, i - WINDOW_SIZE);
            int maxPossible = Math.min(MAX_MATCH, n - i);

            for (int len = 1; len <= maxPossible; len++) {
                int offset = findMatch(originalBytes, searchStart, i - 1, i, len);
                if (offset != -1) {
                    bestOffset = i - offset;
                    bestLength = len;
                } else {
                    break;
                }
            }

            if (bestLength >= 3) {
                output.add((byte) 0x00);
                output.add((byte) bestLength);
                output.add((byte) bestOffset);
                i += bestLength;
            } else {
                output.add(originalBytes[i]);
                i++;
            }
        }

        byte[] result = new byte[output.size()];
        for (int j = 0; j < output.size(); j++) {
            result[j] = output.get(j);
        }
        return result;
    }

    private String decompressBytes(byte[] compressedBytes) {
        List<Byte> result = new ArrayList<>();
        int i = 0;

        while (i < compressedBytes.length) {
            if (compressedBytes[i] == 0x00) {
                int length = compressedBytes[i + 1] & 0xFF;
                int offset = compressedBytes[i + 2] & 0xFF;
                int start  = result.size() - offset;
                for (int j = 0; j < length; j++) {
                    result.add(result.get(start + j));
                }
                i += 3;
            } else {
                result.add(compressedBytes[i]);
                i++;
            }
        }

        byte[] bytes = new byte[result.size()];
        for (int j = 0; j < bytes.length; j++) {
            bytes[j] = result.get(j);
        }
        return new String(bytes);
    }

    private int findMatch(byte[] originalBytes, int izq, int der, int inicio, int len) {
        for (int j = izq; j <= der; j++) {
            boolean match = true;
            for (int k = 0; k < len; k++) {
                if (j + k >= originalBytes.length || inicio + k >= originalBytes.length) {
                    match = false;
                    break;
                }
                if (originalBytes[j + k] != originalBytes[inicio + k]) {
                    match = false;
                    break;
                }
            }
            if (match) return j;
        }
        return -1;
    }
}