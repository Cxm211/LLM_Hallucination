private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value != null) {
      if (value.indexOf("@")==-1) {
        return;
      }
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }