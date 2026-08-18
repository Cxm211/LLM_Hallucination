  private void handleBlockComment(Comment comment) {
    if (comment.getValue().contains("/* @") || comment.getValue().matches("(?s).*\\n\\s*\\*\\s*@.*")) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }