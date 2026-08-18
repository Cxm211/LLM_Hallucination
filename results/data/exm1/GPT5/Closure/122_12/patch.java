  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }
    // Only warn for suspicious annotations in non-JSDoc block comments.
    // JSDoc comments start with '*' (i.e., /** ... */ -> value starts with '*').
    if (!value.startsWith("*") && value.indexOf("@") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }