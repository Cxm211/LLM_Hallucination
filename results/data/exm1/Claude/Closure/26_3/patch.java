private String normalizeSourceName(String filename) {
    if (filenamePrefix != null && filenamePrefix.length() > 0 && filename.indexOf(filenamePrefix) == 0) {
        filename = filename.substring(filenamePrefix.length());
    }
    return filename;
}