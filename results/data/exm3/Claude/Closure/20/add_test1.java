// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testSimpleFunctionCallMultipleArgs() {
  testSame("var a = String(1, 2, 3);");
}