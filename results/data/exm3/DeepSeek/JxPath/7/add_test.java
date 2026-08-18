// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNodeSetOperationsRelational() {
        // Left number, right node set
        assertXPathValue(context, "0 < $array", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "0 <= $array", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "1 > $array", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "1 >= $array", Boolean.TRUE, Boolean.class);
        // False cases
        assertXPathValue(context, "0 > $array", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "0 >= $array", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "1 < $array", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "1 <= $array", Boolean.FALSE, Boolean.class);
        // Both node sets (same set)
        assertXPathValue(context, "$array > $array", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array >= $array", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array < $array", Boolean.TRUE, Boolean.class);
        assertXPathValue(context, "$array <= $array", Boolean.TRUE, Boolean.class);
    }
