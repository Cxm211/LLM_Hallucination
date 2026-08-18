// org/apache/commons/lang/EntitiesTest.java
public void testUpperBoundary() throws Exception {
    doTestUnescapeEntity("&#65535;", "\uFFFF");
    doTestUnescapeEntity("&#65536;", "&#65536;");
    doTestUnescapeEntity("&#xFFFF;", "\uFFFF");
    doTestUnescapeEntity("&#x10000;", "&#x10000;");
}