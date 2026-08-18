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
        count(totalRead);
        
        if (totalRead == -1) {
            hasHitEOF = true;
        } else {
            entryOffset += totalRead;
        }

        return totalRead;
    }

// trigger testcase
public void shouldThrowAnExceptionOnTruncatedEntries() throws Exception {
        File dir = mkdir("COMPRESS-279");
        TarArchiveInputStream is = getTestStream("/COMPRESS-279.tar");
        FileOutputStream out = null;
        try {
            TarArchiveEntry entry = is.getNextTarEntry();
            int count = 0;
            while (entry != null) {
                out = new FileOutputStream(new File(dir, String.valueOf(count)));
                IOUtils.copy(is, out);
                out.close();
                out = null;
                count++;
                entry = is.getNextTarEntry();
            }
        } finally {
            is.close();
            if (out != null) {
                out.close();
            }
        }
    }
