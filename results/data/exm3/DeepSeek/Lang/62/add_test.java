// org/apache/commons/lang/EntitiesTest.java
public void testNegativeNumericEntity() throws Exception {
        doTestUnescapeEntity("&#-1;", "&#-1;");
        doTestUnescapeEntity("&#-123;", "&#-123;");
        doTestUnescapeEntity("x&#-1;y", "x&#-1;y");
    }
