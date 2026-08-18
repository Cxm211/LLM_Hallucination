private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    // Warn if an annotation-like token appears in a non-JSDoc block comment.
    // comment.getValue() does not include the opening "/*", so check for '@'
    // and ensure the comment is not a JSDoc (which would start with '*').
    if (value.indexOf("@") != -1 && (value.isEmpty() || value.charAt(0) != '*')) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }