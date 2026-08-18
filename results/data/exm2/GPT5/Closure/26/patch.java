  private String normalizeSourceName(String filename) {
    if (filename.indexOf(filenamePrefix) == 0) {
      filename = filename.substring(filenamePrefix.length());
    }
    // Normalize dashes to underscores for module names.
    return filename.replace('-', '_');
  }