// buggy function
  private void handleBlockComment(Comment comment) {
    if (comment.getValue().indexOf("/* @") != -1 || comment.getValue().indexOf("\n * @") != -1) {
      errorReporter.warning(
          SUSPICIOUS_COMMENT_WARNING,
          sourceName,
          comment.getLineno(), "", 0);
    }
  }

// trigger testcase
// com/google/javascript/jscomp/parsing/ParserTest.java::testSuspiciousBlockCommentWarning3
public void testSuspiciousBlockCommentWarning3() {
    parse("/* \n *@type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

// com/google/javascript/jscomp/parsing/ParserTest.java::testSuspiciousBlockCommentWarning4
public void testSuspiciousBlockCommentWarning4() {
    parse(
        "  /*\n" +
        "   * @type {number}\n" +
        "   */\n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }

// com/google/javascript/jscomp/parsing/ParserTest.java::testSuspiciousBlockCommentWarning5
public void testSuspiciousBlockCommentWarning5() {
    parse(
        "  /*\n" +
        "   * some random text here\n" +
        "   * @type {number}\n" +
        "   */\n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }
