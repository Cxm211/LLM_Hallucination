private void handleBlockComment(Comment comment) {
  String s = comment.getValue();
  boolean suspicious = false;

  int i = s.indexOf("/*");
  if (i != -1) {
    int j = i + 2;
    while (j < s.length()) {
      char c = s.charAt(j);
      if (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '*') {
        j++;
      } else {
        break;
      }
    }
    if (j < s.length() && s.charAt(j) == '@') {
      suspicious = true;
    }
  }

  if (!suspicious) {
    int idx = 0;
    while (idx < s.length()) {
      int nl = s.indexOf('\n', idx);
      if (nl == -1) {
        break;
      }
      int j = nl + 1;
      while (j < s.length()) {
        char c = s.charAt(j);
        if (c == ' ' || c == '\t') {
          j++;
        } else {
          break;
        }
      }
      if (j < s.length() && s.charAt(j) == '*') {
        j++;
        while (j < s.length()) {
          char c = s.charAt(j);
          if (c == ' ' || c == '\t') {
            j++;
          } else {
            break;
          }
        }
        if (j < s.length() && s.charAt(j) == '@') {
          suspicious = true;
          break;
        }
      }
      idx = nl + 1;
    }
  }

  if (suspicious) {
    errorReporter.warning(
        SUSPICIOUS_COMMENT_WARNING,
        sourceName,
        comment.getLineno(), "", 0);
  }
}