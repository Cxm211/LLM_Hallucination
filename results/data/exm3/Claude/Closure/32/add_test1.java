// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testParsePreserveEmptyLine() throws Exception {
  Node node = new Node(1);
  this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
  String comment = "@preserve\n\nBar*/";
  parse(comment);
  assertEquals("\n\nBar", node.getJSDocInfo().getLicense());
}