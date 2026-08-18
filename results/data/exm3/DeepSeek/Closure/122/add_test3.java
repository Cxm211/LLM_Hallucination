// com/google/javascript/jscomp/parsing/ParserTest.java
public void testSuspiciousBlockCommentWarning9() {
    parse("/*\\n @type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
}
