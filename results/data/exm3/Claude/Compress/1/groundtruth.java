// ===== FIXED org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream :: close() [lines 344-350] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-1-fixed/src/main/java/org/apache/commons/compress/archivers/cpio/CpioArchiveOutputStream.java =====
    public void close() throws IOException {
        if (!this.closed) {
            this.finish();
            super.close();
            this.closed = true;
        }
    }
