// com/google/javascript/jscomp/parsing/ParserTest.java
public void testSuspiciousBlockCommentWarningNegative() {
  parseWarningFree("/* This is a normal comment */ var x = 3;");
}