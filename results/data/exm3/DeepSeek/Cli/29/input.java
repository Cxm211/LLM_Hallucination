// buggy function
    static String stripLeadingAndTrailingQuotes(String str)
    {
        if (str.startsWith("\""))
        {
            str = str.substring(1, str.length());
        }
        int length = str.length();
        if (str.endsWith("\""))
        {
            str = str.substring(0, length - 1);
        }
        
        return str;
    }

// trigger testcase
// org/apache/commons/cli/UtilTest.java::testStripLeadingAndTrailingQuotes
public void testStripLeadingAndTrailingQuotes()
    {
        assertEquals("foo", Util.stripLeadingAndTrailingQuotes("\"foo\""));
        assertEquals("foo \"bar\"", Util.stripLeadingAndTrailingQuotes("foo \"bar\""));
        assertEquals("\"foo\" bar", Util.stripLeadingAndTrailingQuotes("\"foo\" bar"));
        assertEquals("\"foo\" and \"bar\"", Util.stripLeadingAndTrailingQuotes("\"foo\" and \"bar\""));
        assertEquals("\"", Util.stripLeadingAndTrailingQuotes("\""));
    }
