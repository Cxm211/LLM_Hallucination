  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(?:/\\\\*(?!\\\\*)|\\\\n)\\\\\s*\\\\*?\\\\s*@");
    if (pattern.matcher(value).find()) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }