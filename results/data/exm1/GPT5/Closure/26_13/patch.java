  private String normalizeSourceName(String filename) {
    if (filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      int start = filenamePrefix.length();
      if (start < filename.length() && filename.charAt(start) == '/') {
        start++;
      }
      filename = filename.substring(start);
    }
    return filename;
  }
