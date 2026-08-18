private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    // Warn if a block comment appears to contain JSDoc-style annotations
    // but isn't a proper JSDoc (i.e., not starting with '/**').
    if (value.indexOf("/* @") != -1 ||
        value.indexOf("\n * @") != -1 ||
        value.indexOf("\r\n * @") != -1 ||
        value.indexOf("* @") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }