// org/apache/commons/cli/UtilTest.java
public void testStripLeadingAndTrailingQuotesAdditional() {
    assertEquals("\"\"foo", Util.stripLeadingAndTrailingQuotes("\"\"foo"));
    assertEquals("foo\"\"", Util.stripLeadingAndTrailingQuotes("foo\"\""));
}
