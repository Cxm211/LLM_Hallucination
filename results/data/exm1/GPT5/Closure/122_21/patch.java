  private void handleBlockComment(Comment comment) {
    String s = comment.getValue();
    if (s.indexOf("@") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }