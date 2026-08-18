  private void handleBlockComment(Comment comment) {
    if (comment.getValue().indexOf("@") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }