// org/apache/commons/jxpath/ri/compiler/CoreOperationTest.java
public void testNanBothSides() {
        // NaN on left
        assertXPathValue(context, "$nan > 5", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan < 5", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "$nan = 5", Boolean.FALSE, Boolean.class);
        // NaN on right
        assertXPathValue(context, "5 > $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "5 < $nan", Boolean.FALSE, Boolean.class);
        assertXPathValue(context, "5 = $nan", Boolean.FALSE, Boolean.class);
    }
