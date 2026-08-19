// ===== FIXED org.apache.commons.compress.archivers.tar.TarUtils :: verifyCheckSum(byte[]) [lines 593-608] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-35-fixed/src/main/java/org/apache/commons/compress/archivers/tar/TarUtils.java =====
    public static boolean verifyCheckSum(byte[] header) {
        long storedSum = parseOctal(header, CHKSUM_OFFSET, CHKSUMLEN);
        long unsignedSum = 0;
        long signedSum = 0;

        int digits = 0;
        for (int i = 0; i < header.length; i++) {
            byte b = header[i];
            if (CHKSUM_OFFSET  <= i && i < CHKSUM_OFFSET + CHKSUMLEN) {
                b = ' ';
            }
            unsignedSum += 0xff & b;
            signedSum += b;
        }
        return storedSum == unsignedSum || storedSum == signedSum;
    }
