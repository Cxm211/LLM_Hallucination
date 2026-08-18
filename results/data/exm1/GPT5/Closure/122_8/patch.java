  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }
    // Do not warn on proper JSDoc comments (/** ... */)
    if (value.startsWith("/**")) {
      return;
    }
    if (value.indexOf("/* @") != -1 || value.indexOf("\n * @") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }