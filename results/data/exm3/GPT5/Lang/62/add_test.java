// org/apache/commons/lang/EntitiesTest.java
public void testNumberOverflowBoundary() throws Exception {
        doTestUnescapeEntity("&#65536;", "&#65536;");
        doTestUnescapeEntity("&#x10000;", "&#x10000;");
    }