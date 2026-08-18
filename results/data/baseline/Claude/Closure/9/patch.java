private String normalizeSourceName(String filename) {
    // The DOS command shell will normalize "/" to "\", so we have to
    // wrestle it back.

    if (filename != null && filenamePrefix != null && filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }

    return filename;
  }