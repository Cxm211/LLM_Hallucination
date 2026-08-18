private String normalizeSourceName(String filename) {
    if (filename == null) {
      return null;
    }
    String name = filename;
    String prefix = filenamePrefix;
    // Normalize path separators to ensure consistent prefix matching
    name = name.replace('\\', '/');
    if (prefix != null) {
      String normPrefix = prefix.replace('\\', '/');
      if (name.startsWith(normPrefix)) {
        name = name.substring(normPrefix.length());
      }
    }
    return name;
  }