private String normalizeSourceName(String filename) {
    if (filename != null && filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      return filename.substring(filenamePrefix.length());
    }
    return filename;
  }