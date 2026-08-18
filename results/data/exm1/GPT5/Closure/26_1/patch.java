  private String normalizeSourceName(String filename) {
    if (filename == null) {
      return null;
    }
    String prefix = filenamePrefix;
    if (prefix != null && !prefix.isEmpty() && filename.startsWith(prefix)) {
      filename = filename.substring(prefix.length());
    }
    return filename;
  }
