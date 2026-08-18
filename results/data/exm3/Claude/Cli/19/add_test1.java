// org/apache/commons/cli/PosixParserTest.java
public void testUnrecognizedOptionAtEnd() throws Exception
{
    String[] args = new String[] { "-a", "-b", "-invalid" };

    try
    {
        parser.parse(options, args);
        fail("UnrecognizedOptionException wasn't thrown");
    }
    catch (UnrecognizedOptionException e)
    {
        assertEquals("-invalid", e.getOption());
    }
}