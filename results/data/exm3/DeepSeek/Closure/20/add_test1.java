// com/google/javascript/jscomp/PeepholeSubstituteAlternateSyntaxTest.java
public void testSimpleFunctionCallNonLiteral() {
    testSame("var a = String([]);");
  }
