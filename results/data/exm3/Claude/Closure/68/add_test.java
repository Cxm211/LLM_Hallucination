// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testIssue477_missingOpeningLPAfterStringType() throws Exception {
    parse("@type {function} */",
        "Bad type annotation. missing opening (");
  }