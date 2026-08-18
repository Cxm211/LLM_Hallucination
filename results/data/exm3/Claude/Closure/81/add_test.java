// com/google/javascript/jscomp/parsing/ParserTest.java
public void testUnnamedFunctionInForLoop() {
    parseError("for (var i = 0; i < 10; i++) { function() {}; }", "unnamed function statement");
  }