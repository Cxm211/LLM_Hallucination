// ===== FIXED org.apache.commons.compress.archivers.zip.ZipArchiveEntry :: ZipArchiveEntry [lines 62-65] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-6-fixed/src/main/java/org/apache/commons/compress/archivers/zip/ZipArchiveEntry.java =====
    public ZipArchiveEntry(String name) {
        super(name);
        setName(name);
    }

// ===== FIXED org.apache.commons.compress.archivers.zip.ZipArchiveEntry :: equals(Object) [lines 455-473] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-6-fixed/src/main/java/org/apache/commons/compress/archivers/zip/ZipArchiveEntry.java =====
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ZipArchiveEntry other = (ZipArchiveEntry) obj;
        String myName = getName();
        String otherName = other.getName();
        if (myName == null) {
            if (otherName != null) {
                return false;
            }
        } else if (!myName.equals(otherName)) {
            return false;
        }
        return true;
    }
