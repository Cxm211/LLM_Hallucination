private String normalizeSourceName(String filename) {
  if (filenamePrefix != null && !filenamePrefix.isEmpty() && filename.startsWith(filenamePrefix)) {
    filename = filename.substring(filenamePrefix.length());
  }
  return filename;
}