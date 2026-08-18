// com/google/javascript/jscomp/parsing/ParserTest.java
public void testSuspiciousBlockCommentWarning7() {
  parse("/* \n *  @param {string} x */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
}