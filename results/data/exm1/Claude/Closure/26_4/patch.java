private String normalizeSourceName(String filename) {
    if (filenamePrefix != null && !filenamePrefix.isEmpty() && filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }