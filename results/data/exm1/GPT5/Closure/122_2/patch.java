private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    // comment.getValue() does not include the opening /*, so check for patterns
    // that would indicate a jsdoc-style tag within a non-jsdoc block comment.
    if (value.indexOf("\n * @") != -1 || value.startsWith(" * @") || value.startsWith("* @")) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }