// com/google/javascript/jscomp/parsing/ParserTest.java::testSuspiciousBlockCommentWarningNewSingleLine
public void testSuspiciousBlockCommentWarningNewSingleLine() {
    parse("/* @type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }