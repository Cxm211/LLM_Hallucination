  private void handleBlockComment(Comment comment) {
    if (comment.getValue().indexOf("/* @") != -1 || comment.getValue().indexOf("\n * @") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }

// trigger testcase
public void testSuspiciousBlockCommentWarning3() {
    parse("/* \n *@type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

public void testSuspiciousBlockCommentWarning4() {
    parse(
        "  /*\n" +
        "   * @type {number}\n" +
        "   */\n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }

public void testSuspiciousBlockCommentWarning5() {
    parse(
        "  /*\n" +
        "   * some random text here\n" +
        "   * @type {number}\n" +
        "   */\n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }
