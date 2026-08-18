  private String normalizeSourceName(String filename) {
    // The DOS command shell will normalize "/" to "\", so we have to
    // wrestle it back.

    if (filename == null) {
      return null;
    }

    // Normalize Windows-style separators to POSIX-style.
    filename = filename.replace('\\', '/');

    if (filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
      // Remove a leading path separator after stripping the prefix.
      if (!filename.isEmpty() && filename.charAt(0) == '/') {
        filename = filename.substring(1);
      }
    }

    return filename;
  }