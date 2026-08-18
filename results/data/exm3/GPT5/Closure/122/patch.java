private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    if (value == null) {
      return;
    }
    boolean suspicious = false;
    int start = 0;
    int len = value.length();
    while (start <= len) {
      int end = value.indexOf('\n', start);
      if (end == -1) {
        end = len;
      }
      String line = value.substring(start, end);
      int i = 0;
      // Skip leading whitespace
      while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
        i++;
      }
      // Optional leading '*'
      if (i < line.length() && line.charAt(i) == '*') {
        i++;
        while (i < line.length() && (line.charAt(i) == ' ' || line.charAt(i) == '\t')) {
          i++;
        }
      }
      if (i < line.length() && line.charAt(i) == '@') {
        suspicious = true;
        break;
      }
      if (end == len) {
        break;
      }
      start = end + 1;
    }
    if (suspicious) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }