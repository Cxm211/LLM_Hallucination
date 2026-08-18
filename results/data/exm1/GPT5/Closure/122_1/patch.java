private void handleBlockComment(Comment comment) {
  String value = comment.getValue();
  if (value == null) {
    return;
  }
  String v = value.replace("\r\n", "\n");
  if (v.startsWith("*")) {
    // Likely a JSDoc (/** ... */). Do not warn.
    return;
  }
  if (v.indexOf("/* @") != -1 || v.indexOf("\n * @") != -1) {
    errorReporter.warning(
        SUSPICIOUS_COMMENT_WARNING,
        sourceName,
        comment.getLineno(), "", 0);
  }
}