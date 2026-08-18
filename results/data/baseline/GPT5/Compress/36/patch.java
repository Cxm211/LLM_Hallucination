private InputStream getCurrentStream() throws IOException {
        if (deferredBlockStreams.isEmpty()) {
            throw new IllegalStateException("No current 7z entry (call getNextEntry() first).");
        }
        
        while (deferredBlockStreams.size() > 1) {
            // In solid compression mode we need to decompress all leading folder'
            // streams to get access to an entry. We defer this until really needed
            // so that entire blocks can be skipped without wasting time for decompression.
            final InputStream stream = deferredBlockStreams.remove(0);
            byte[] buffer = new byte[8192];
            while (stream.read(buffer) != -1) {
                // drain stream fully
            }
            stream.close();
        }

        return deferredBlockStreams.get(0);
    }