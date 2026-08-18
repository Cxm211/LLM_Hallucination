private void handleBlockComment(Comment comment) {
    String value = comment.getValue();
    boolean suspicious = false;
    if (value.indexOf("/*@") != -1 || value.indexOf("/* @") != -1) {
      suspicious = true;
    } else {
      int index1 = value.indexOf("* @");
      if (index1 != -1 && value.lastIndexOf('\n', index1) != -1) {
        suspicious = true;
      } else {
        int index2 = value.indexOf("*@");
        if (index2 != -1 && value.lastIndexOf('\n', index2) != -1) {
          suspicious = true;
        }
      }
    }
    if (suspicious) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }