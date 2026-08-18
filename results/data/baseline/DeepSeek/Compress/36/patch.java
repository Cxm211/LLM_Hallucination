private InputStream getCurrentStream() throws IOException {
    if (deferredBlockStreams.isEmpty()) {
        throw new IllegalStateException("No current 7z entry (call getNextEntry() first).");
    }
    
    while (deferredBlockStreams.size() > 1) {
        final InputStream stream = deferredBlockStreams.remove(0);
        try {
            IOUtils.skip(stream, Long.MAX_VALUE);
        } finally {
            stream.close();
        }
    }

    return deferredBlockStreams.get(0);
}