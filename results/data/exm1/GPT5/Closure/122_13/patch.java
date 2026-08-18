private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }
    // Warn if a non-Javadoc block comment appears to contain JSDoc tags.
    // Comment.getValue() usually excludes the /* */ delimiters, so look for lines
    // that start with optional whitespace/asterisk followed by an @.
    java.util.regex.Pattern p = java.util.regex.Pattern.compile("(?m)^(?:\\s*\\*)?\\s*@");
    if (p.matcher(value).find()) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }