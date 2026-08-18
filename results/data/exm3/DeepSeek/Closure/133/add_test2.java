// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testTextExtentsMultipleContinuationLines() {
    parse("@return {@code foo} bar\n * baz\n * qux */", true, "Bad type annotation. type not recognized due to syntax error");
  }
