// com/google/javascript/jscomp/parsing/JsDocInfoParserTest.java
public void testParseLicenseWithAnnotationAtStart() throws Exception {
  Node node = new Node(1);
  this.fileLevelJsDocBuilder = node.getJsDocBuilderForNode();
  String comment = "@license @author Charlie Brown \n * Some text */";
  parse(comment);
  assertEquals(" @author Charlie Brown \n Some text ",
      node.getJSDocInfo().getLicense());
}