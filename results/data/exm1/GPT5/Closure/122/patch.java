  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }
    if (java.util.regex.Pattern.compile("(?m)(^|\\n)\\s*\\*?\\s*@").matcher(value).find()) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }