// ===== FIXED org.apache.commons.compress.utils.IOUtils :: skip(InputStream, long) [lines 94-116] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-26-fixed/src/main/java/org/apache/commons/compress/utils/IOUtils.java =====
    public static long skip(InputStream input, long numToSkip) throws IOException {
        long available = numToSkip;
        while (numToSkip > 0) {
            long skipped = input.skip(numToSkip);
            if (skipped == 0) {
                break;
            }
            numToSkip -= skipped;
        }
            
        if (numToSkip > 0) {
            byte[] skipBuf = new byte[SKIP_BUF_SIZE];
            while (numToSkip > 0) {
                int read = readFully(input, skipBuf, 0,
                                     (int) Math.min(numToSkip, SKIP_BUF_SIZE));
                if (read < 1) {
                    break;
                }
                numToSkip -= read;
            }
        }
        return available - numToSkip;
    }
