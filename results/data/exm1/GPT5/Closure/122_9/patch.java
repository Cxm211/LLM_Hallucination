private void handleBlockComment(Comment comment) {
    String text = comment.getValue();
    if (!text.startsWith("/**") && text.indexOf("@") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }