// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testIssue477_validFunction() throws Exception {
    JSDocInfo info = parse("@type {function(string):number} */");
    assertTypeEquals(createFunctionTypeNode(), info.getType().getRoot());
  }