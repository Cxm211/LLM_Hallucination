// com/google/javascript/jscomp/parsing/ParserTest.java
public void testForEachNoVar() {
    parseError(
        "for each (x in [1,2,3]) { }",
        "unsupported language extension: for each");
  }
