// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testSimpleFunctionCallNoArgs() {
  testSame("var a = String();");
}