private String normalizeSourceName(String filename) {
    // The DOS command shell will normalize "/" to "\\", so we have to
    // wrestle it back.
    if (filename == null) {
      return null;
    }

    // Normalize path separators to forward slashes for consistent processing.
    String normalized = filename.replace('\\', '/');
    String prefix = filenamePrefix != null ? filenamePrefix.replace('\\', '/') : null;

    if (prefix != null && !prefix.isEmpty() && normalized.startsWith(prefix)) {
      normalized = normalized.substring(prefix.length());
    }

    return normalized;
  }