// ===== FIXED org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp :: unixTimeToZipLong(long) [lines 528-533] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-46-fixed/src/main/java/org/apache/commons/compress/archivers/zip/X5455_ExtendedTimestamp.java =====
    private static ZipLong unixTimeToZipLong(long l) {
        if (l < Integer.MIN_VALUE || l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("X5455 timestamps must fit in a signed 32 bit integer: " + l);
        }
        return new ZipLong(l);
    }
