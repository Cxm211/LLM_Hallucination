// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java::testEmptyNodeSetOperationsRightHand
public void testEmptyNodeSetOperationsRightHand() {
        assertXPathValue(context, "0 != /idonotexist", Boolean.FALSE, Boolean.class);
    }