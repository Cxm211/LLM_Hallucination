// org/apache/commons/cli/UtilTest.java
public void testStripLeadingAndTrailingQuotesTrailingOnly()
{
    assertEquals("foo", Util.stripLeadingAndTrailingQuotes("foo\""));
}