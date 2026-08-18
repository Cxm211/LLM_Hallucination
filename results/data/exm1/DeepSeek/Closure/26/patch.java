private String normalizeSourceName(String filename) {
    if (filenamePrefix == null) {
      return filename;
    }
    if (filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }