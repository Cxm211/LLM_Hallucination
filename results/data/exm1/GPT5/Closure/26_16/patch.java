private String normalizeSourceName(String filename) {
    if (filename != null && filenamePrefix != null && filename.startsWith(filenamePrefix)) {
      String suffix = filename.substring(filenamePrefix.length());
      // If a replacement prefix is configured, use it; otherwise strip the prefix.
      if (filenamePrefixReplacement != null) {
        return filenamePrefixReplacement + suffix;
      }
      return suffix;
    }
    return filename;
  }