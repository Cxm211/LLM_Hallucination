private void handleBlockComment(Comment comment) {
    String s = comment.getValue();
    if (s == null) {
      return;
    }
    // Only warn if an annotation-like '@' appears in a non-JSDoc block comment.
    // JSDoc comments start with '/**', which typically means the comment text
    // (without delimiters) begins with '*'. Allow leading whitespace before '*'.
    if (s.indexOf('@') != -1) {
      int i = 0;
      while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
        i++;
      }
      if (i >= s.length() || s.charAt(i) != '*') {
        errorReporter.warning(
            SUSPICIOUS_COMMENT_WARNING,
            sourceName,
            comment.getLineno(), "", 0);
      }
    }
  }