private void handleBlockComment(Comment comment) {
  if (comment.getValue().matches("^\\s*@.*") || comment.getValue().indexOf("\n * @") != -1) {
    errorReporter.warning(
        SUSPICIOUS_COMMENT_WARNING,
        sourceName,
        comment.getLineno(), "", 0);
  }
}