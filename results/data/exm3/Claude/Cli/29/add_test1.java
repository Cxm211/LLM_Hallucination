// org/apache/commons/cli/UtilTest.java
public void testStripLeadingAndTrailingQuotesNoQuotes()
{
    assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo"));
}