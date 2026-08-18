// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testParseLicenseOnlyWhitespace() throws Exception {
  Node node = new Node(1);
  this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
  String comment = "@license\n   \nFoo*/";
  parse(comment);
  assertEquals("\n\nFoo", node.getJSDocInfo().getLicense());
}