    private InputStream getCurrentStream() throws IOException {
        if (deferredBlockStreams.isEmpty()) {
            throw new IllegalStateException("No current 7z entry (call getNextEntry() first).");
        }
        return deferredBlockStreams.get(0);
    }