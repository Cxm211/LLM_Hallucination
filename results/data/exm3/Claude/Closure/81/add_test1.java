// com/google/javascript/jscomp/parsing/ParserTest.java
public void testUnnamedFunctionInWhileLoop() {
    parseError("while (true) { function() {}; }", "unnamed function statement");
  }