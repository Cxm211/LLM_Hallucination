  private String normalizeSourceName(String filename) {
    if (filenamePrefix != null && filename.indexOf(filenamePrefix) == 0) {
      int prefixLen = filenamePrefix.length();
      if (prefixLen == filename.length() || filename.charAt(prefixLen) == '/') {
        filename = filename.substring(prefixLen);
      }
    }
    return filename;
  }