  private String normalizeSourceName(String filename) {
    // The DOS command shell will normalize "/" to "\\", so we have to
    // wrestle it back.
    if (filename == null) {
      return null;
    }
    filename = filename.replace('\\', '/');
    if (filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      filename = filename.substring(filenamePrefix.length());
    }
    return filename;
  }