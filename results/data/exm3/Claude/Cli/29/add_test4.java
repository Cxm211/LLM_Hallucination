// org/apache/commons/cli/UtilTest.java
public void testStripLeadingAndTrailingQuotesDoubleQuote()
{
    assertEquals("\"\"", Util.stripLeadingAndTrailingQuotes("\"\"\"\""));
}