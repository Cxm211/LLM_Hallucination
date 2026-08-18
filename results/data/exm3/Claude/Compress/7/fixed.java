// ===== FIXED org.apache.commons.compress.archivers.tar.TarUtils :: parseName(byte[], int, int) [lines 93-106] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-7-fixed/src/main/java/org/apache/commons/compress/archivers/tar/TarUtils.java =====
    public static String parseName(byte[] buffer, final int offset, final int length) {
        StringBuffer result = new StringBuffer(length);
        int          end = offset + length;

        for (int i = offset; i < end; ++i) {
            byte b = buffer[i];
            if (b == 0) { // Trailing null
                break;
            }
            result.append((char) (b & 0xFF)); // Allow for sign-extension
        }

        return result.toString();
    }
