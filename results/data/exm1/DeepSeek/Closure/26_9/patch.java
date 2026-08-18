private String normalizeSourceName(String filename) {
    if (filename == null) return null;
    if (filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }