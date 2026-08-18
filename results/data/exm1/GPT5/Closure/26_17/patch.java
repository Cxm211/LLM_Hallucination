  private String normalizeSourceName(String filename) {
    if (filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }
