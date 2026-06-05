package network;

import java.io.ByteArrayOutputStream;

public final class LZ77Compressor {
    private static final int WINDOW_SIZE = 4096;
    private static final int LOOKAHEAD_SIZE = 18;
    private static final int MIN_MATCH = 3;

    private LZ77Compressor() {
    }

    public static byte[] compress(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int pos = 0;
        while (pos < input.length) {
            int maxMatch = 0;
            int maxOffset = 0;
            int start = Math.max(0, pos - WINDOW_SIZE);
            for (int i = start; i < pos; i++) {
                int matchLen = 0;
                while (matchLen < LOOKAHEAD_SIZE
                        && pos + matchLen < input.length
                        && input[i + matchLen] == input[pos + matchLen]) {
                    matchLen++;
                }
                if (matchLen > maxMatch) {
                    maxMatch = matchLen;
                    maxOffset = pos - i;
                }
            }

            if (maxMatch >= MIN_MATCH) {
                out.write(1);
                out.write((maxOffset >> 8) & 0xFF);
                out.write(maxOffset & 0xFF);
                out.write(maxMatch & 0xFF);
                pos += maxMatch;
            } else {
                out.write(0);
                out.write(input[pos]);
                pos++;
            }
        }
        return out.toByteArray();
    }

    public static byte[] decompress(byte[] input) {
        if (input == null || input.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int pos = 0;
        while (pos < input.length) {
            int flag = input[pos++] & 0xFF;
            if (flag == 0) {
                out.write(input[pos++]);
            } else {
                int offset = ((input[pos++] & 0xFF) << 8) | (input[pos++] & 0xFF);
                int length = input[pos++] & 0xFF;
                byte[] buffer = out.toByteArray();
                int start = buffer.length - offset;
                for (int i = 0; i < length; i++) {
                    out.write(buffer[start + i]);
                }
            }
        }
        return out.toByteArray();
    }
}
