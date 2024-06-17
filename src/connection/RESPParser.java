package connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Objects;

public class RESPParser {

    private static final String CRLF = "\r\n";

    public static String OKAY = "OK";
    public static Integer NULL_VALUE= -1;

    /**
     * Decodes a RESP (REdis Serialization Protocol) encoded input line.
     *
     * @param reader    BufferedReader to read additional lines for multi-line responses.
     * @param inputLine Input line to decode.
     * @return Decoded array of strings corresponding to the RESP type.
     * @throws IOException If there are issues reading from the reader or decoding the input.
     */
    public static String[] decode(BufferedReader reader, String inputLine) throws IOException {
        char type = inputLine.charAt(0);
        switch (type) {
            case '+':
            case '-':
            case ':':
                // Simple strings (+), errors (-), integers (:)
                return new String[]{inputLine.substring(1)};
            case '$':
                // Bulk strings ($)
                if (isNullString(inputLine)) return new String[]{null}; // Check for null bulk string
                return new String[]{reader.readLine()};
            case '*':
                // Arrays (*)
                int length = Integer.parseInt(inputLine.substring(1));
                String[] array = new String[length];
                for (int i = 0; i < length; i++) {
                    // Recursively decode each element in the array
                    array[i] = Objects.requireNonNull(decode(reader, reader.readLine()))[0];
                }
                return array;
            default:
                throw new IOException("Unknown RESP type: " + type);
        }
    }

    public static <T> String encode(T value) {
        // Handling null case
        if (value == null) {
            return "$-1" + CRLF;
        }

        // Switch on the class of the result object for specific types
        switch (value.getClass().getSimpleName()) {
            case "String":
                return "+" + value + CRLF;
            case "Integer":
            case "Long":
                return ":" + value + CRLF;
            default:
                // Handling other cases with if-else
                if (value instanceof Error) {
                    return "-" + ((Error)value).getMessage() + CRLF;
                } else if (value instanceof String[]) {
                    return encodeArray((String[]) value);
                } else {
                    throw new IllegalArgumentException("Unsupported type for RESP encoding: " + value.getClass());
                }
        }
    }
    private static String encodeString(String value) {
        return "+" + value + "\r\n";
    }

    private static String encodeArray(String[] array) {
        StringBuilder sb = new StringBuilder();
        sb.append("*").append(array.length).append("\r\n");
        for (String element : array) {
            sb.append(encodeString(element));
        }
        return sb.toString();
    }

    /**
     * Checks if a RESP encoded string represents a null bulk string ($-1).
     *
     * @param data RESP encoded string to check.
     * @return true if the string represents a null bulk string, false otherwise.
     */
    public static boolean isNullString(String data) {
        return data.equals("$-1");
    }
}

