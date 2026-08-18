  private void handleBlockComment(Comment comment) {
    String val = comment.getValue();
    if (val.trim().startsWith("@") || val.contains("\n * @")) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }