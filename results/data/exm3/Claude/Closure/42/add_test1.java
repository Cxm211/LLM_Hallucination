// com/google/javascript/jscomp/parsing/ParserTest.java
public void testForEachError() {
  parseError(
      "for each (x in [1,2,3]) { x; }",
      "unsupported language extension: for each");
}