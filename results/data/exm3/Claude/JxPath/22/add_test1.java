// org/apache/commons/jxpath/ri/model/JXPath154Test.java
public void testNestedEmptyNamespaceDOM() {
    doTest("b:foo/c:bar[@xmlns:c='']/test", DocumentContainer.MODEL_DOM, "/b:foo[1]/c:bar[1]/test[1]");
}