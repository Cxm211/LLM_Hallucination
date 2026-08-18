// com/google/javascript/jscomp/InlineVariablesTest.java
public void testInlineNonConstantsUninitializedVarTwoReads() {
    testSame("function f() { var a; a; a; }");
  }
