private void handleBlockComment(Comment comment) {
  String value = comment.getValue();
  if ((value.length() > 0 && value.charAt(0) == '@') || value.indexOf(" @") == 0 || value.indexOf("\n * @") != -1) {
    errorReporter.warning(
        SUSPICIOUS_COMMENT_WARNING,
        sourceName,
        comment.getLineno(), "", 0);
  }
}