// org/apache/commons/cli/UtilTest.java
public void testStripLeadingHyphensTripleHyphen() {
    assertEquals("-foo", Util.stripLeadingHyphens("---foo"));
}