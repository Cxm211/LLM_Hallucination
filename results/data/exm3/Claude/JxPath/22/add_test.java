// org/apache/commons/jxpath/ri/model/JXPath154Test.java
public void testEmptyNamespaceAttrDOM() {
    doTest("b:foo[@xmlns:c='']/c:test", DocumentContainer.MODEL_DOM, null);
}