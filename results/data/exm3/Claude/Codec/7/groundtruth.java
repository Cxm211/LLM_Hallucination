// ===== FIXED org.apache.commons.codec.binary.Base64 :: encodeBase64String(byte[]) [lines 669-671] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Codec/Codec-7-fixed/src/java/org/apache/commons/codec/binary/Base64.java =====
    public static String encodeBase64String(byte[] binaryData) {
        return StringUtils.newStringUtf8(encodeBase64(binaryData, false));
    }
