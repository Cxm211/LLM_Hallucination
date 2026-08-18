private String normalizeSourceName(String filename) {
    if (filename == null) {
      return null;
    }
    if (filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }