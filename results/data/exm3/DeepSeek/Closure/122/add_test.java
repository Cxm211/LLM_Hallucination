// com/google/javascript/jscomp/parsing/ParserTest.java
public void testSuspiciousBlockCommentWarning6() {
    parse("/*@type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
}
