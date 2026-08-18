// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testTextExtentsSingleLine() {
    parse("@return {@code foo} */", true, "Bad type annotation. type not recognized due to syntax error");
  }
