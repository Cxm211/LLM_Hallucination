// org/apache/commons/lang/EntitiesTest.java
public void testNegativeNumberOverflow() throws Exception {
    doTestUnescapeEntity("&#-1;", "&#-1;");
    doTestUnescapeEntity("&#x-1;", "&#x-1;");
}