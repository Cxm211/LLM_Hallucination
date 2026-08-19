// ===== FIXED org.apache.commons.compress.archivers.tar.TarArchiveInputStream :: read(byte[], int, int) [lines 569-595] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Compress/Compress-28-fixed/src/main/java/org/apache/commons/compress/archivers/tar/TarArchiveInputStream.java =====
    public int read(byte[] buf, int offset, int numToRead) throws IOException {
    	int totalRead = 0;

        if (hasHitEOF || entryOffset >= entrySize) {
            return -1;
        }

        if (currEntry == null) {
            throw new IllegalStateException("No current tar entry");
        }

        numToRead = Math.min(numToRead, available());
        
        totalRead = is.read(buf, offset, numToRead);
        
        if (totalRead == -1) {
            if (numToRead > 0) {
                throw new IOException("Truncated TAR archive");
            }
            hasHitEOF = true;
        } else {
            count(totalRead);
            entryOffset += totalRead;
        }

        return totalRead;
    }
