// org/apache/commons/cli/UtilTest.java
public void testStripLeadingHyphensNoHyphens() {
    assertEquals("foo", Util.stripLeadingHyphens("foo"));
}