    private InputStream getCurrentStream() throws IOException {
        if (deferredBlockStreams.isEmpty()) {
            throw new IllegalStateException("No current 7z entry (call getNextEntry() first).");
        }
        
        while (deferredBlockStreams.size() > 1) {
            // In solid compression mode we need to decompress all leading folder'
            // streams to get access to an entry. We defer this until really needed
            // so that entire blocks can be skipped without wasting time for decompression.
            final InputStream stream = deferredBlockStreams.remove(0);
            IOUtils.skip(stream, Long.MAX_VALUE);
            stream.close();
        }

        if (currentEntry != null && currentEntry.getSize() == 0) {
            return new java.io.ByteArrayInputStream(new byte[0]);
        }

        return deferredBlockStreams.get(0);
    }