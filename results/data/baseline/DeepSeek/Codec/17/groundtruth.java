// ===== FIXED org.apache.commons.codec.binary.StringUtils :: newStringIso8859_1(byte[]) [lines 338-340] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Codec/Codec-17-fixed/src/main/java/org/apache/commons/codec/binary/StringUtils.java =====
    public static String newStringIso8859_1(final byte[] bytes) {
        return newString(bytes, Charsets.ISO_8859_1);
    }
