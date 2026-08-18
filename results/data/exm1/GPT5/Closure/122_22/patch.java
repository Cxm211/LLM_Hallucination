  private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }

    boolean startsWithAt = false;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (!Character.isWhitespace(c)) {
        startsWithAt = (c == '@');
        break;
      }
    }

    if (startsWithAt || value.indexOf("\n * @") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }