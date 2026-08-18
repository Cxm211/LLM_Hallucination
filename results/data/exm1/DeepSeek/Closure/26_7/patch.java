private String normalizeSourceName(String filename) {
    if (filename != null && filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }