// org/apache/commons/jxpath/ri/model/JXPath154Test.java::testInnerEmptyNamespaceDOM
public void testAttributeNamespaceDOM() {
        // Ensure attribute access on DOM does not cause namespace resolution to fail
        doTest("b:foo/@name", DocumentContainer.MODEL_DOM, "/b:foo[1]/@name");
    }