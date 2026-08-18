  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    // Only warn for suspicious annotations in non-JSDoc block comments.
    if (value.indexOf("/**") == -1 &&
        (value.indexOf("/* @") != -1 || value.indexOf("\n * @") != -1)) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }