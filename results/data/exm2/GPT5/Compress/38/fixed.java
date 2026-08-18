// ===== FIXED org.apache.commons.compress.archivers.tar.TarArchiveEntry :: isDirectory() [lines 850-864] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-38-fixed/src/main/java/org/apache/commons/compress/archivers/tar/TarArchiveEntry.java =====
    public boolean isDirectory() {
        if (file != null) {
            return file.isDirectory();
        }

        if (linkFlag == LF_DIR) {
            return true;
        }

        if (!isPaxHeader() && !isGlobalPaxHeader() && getName().endsWith("/")) {
            return true;
        }

        return false;
    }
