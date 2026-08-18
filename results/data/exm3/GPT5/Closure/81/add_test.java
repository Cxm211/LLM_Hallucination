// com/google/javascript/jscomp/parsing/ParserTest.java
public void testUnnamedFunctionStatementNoBlock() {
    parseError("if (true) function() {};", "unnamed function statement");
  }