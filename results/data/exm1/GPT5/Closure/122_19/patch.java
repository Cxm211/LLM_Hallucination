  private void handleBlockComment(Comment comment) {
    String s = comment.getValue();
    if (s == null) {
      return;
    }
    boolean isJsDoc = s.length() > 0 && s.charAt(0) == '*';
    if (!isJsDoc && s.indexOf('@') != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }
