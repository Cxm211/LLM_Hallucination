// com/google/javascript/jscomp/parsing/ParserTest.java
public void testUnnamedFunctionInTryCatch() {
    parseError("try { function() {}; } catch (e) {}", "unnamed function statement");
  }