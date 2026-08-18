// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testTextExtentsWithInlineTag() {
  parse("@throws {Error} {@link Something} description\n *    more text */",
      true, "Bad type annotation. type not recognized due to syntax error");
}