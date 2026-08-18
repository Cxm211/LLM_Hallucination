// ===== FIXED org.apache.commons.compress.utils.ChecksumCalculatingInputStream :: ChecksumCalculatingInputStream [lines 33-45] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-44-fixed/src/main/java/org/apache/commons/compress/utils/ChecksumCalculatingInputStream.java =====
    public ChecksumCalculatingInputStream(final Checksum checksum, final InputStream in) {

        if ( checksum == null ){
            throw new NullPointerException("Parameter checksum must not be null");
        }

        if ( in == null ){
            throw new NullPointerException("Parameter in must not be null");
        }

        this.checksum = checksum;
        this.in = in;
    }
