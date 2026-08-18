// com/google/javascript/jscomp/parsing/ParserTest.java
public void testUnnamedFunctionStatementNewline() {
    parseError("function\n() {};", "unnamed function statement");
  }
