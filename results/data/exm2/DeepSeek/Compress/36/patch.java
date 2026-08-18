private InputStream getCurrentStream() throws IOException {
    if (deferredBlockStreams.isEmpty()) {
        return new java.io.ByteArrayInputStream(new byte[0]);
    }
    
    while (deferredBlockStreams.size() > 1) {
        final InputStream stream = deferredBlockStreams.remove(0);
        org.apache.commons.compress.utils.IOUtils.skip(stream, Long.MAX_VALUE);
        stream.close();
    }

    return deferredBlockStreams.get(0);
}