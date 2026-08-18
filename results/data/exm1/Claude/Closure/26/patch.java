private String normalizeSourceName(String filename) {
  if (filename.startsWith(filenamePrefix)) {
    filename = filename.substring(filenamePrefix.length());
  }
  return filename;
}