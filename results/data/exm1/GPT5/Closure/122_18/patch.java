  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }
    // Only warn for non-JSDoc block comments that appear to contain annotations.
    boolean isJsDoc = value.startsWith("/**");
    if (!isJsDoc && (value.indexOf("/* @") != -1 || value.indexOf("\n * @") != -1)) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }