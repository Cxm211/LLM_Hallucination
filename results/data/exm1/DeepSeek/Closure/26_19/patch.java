private String normalizeSourceName(String filename) {
    if (filenamePrefix != null && filename != null && filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
}