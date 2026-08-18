// org/apache/commons/cli/UtilTest.java
public void testStripLeadingAndTrailingQuotesLeadingOnly()
{
    assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo"));
}