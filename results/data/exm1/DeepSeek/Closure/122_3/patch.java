private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value.startsWith("/*") && !value.startsWith("/**")) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }