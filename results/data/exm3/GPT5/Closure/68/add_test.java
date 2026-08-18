// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java::testIssue477_newline
public void testIssue477_newline() throws Exception {
    parse("@type function\n() */",
        "Bad type annotation. missing opening (");
  }