// org/apache/commons/cli/PosixParserTest.java
public void testUnrecognizedOptionWithStopAtNonOption() throws Exception
{
    String[] args = new String[] { "-a", "-unknown", "foo" };

    try
    {
        parser.parse(options, args, false);
        fail("UnrecognizedOptionException wasn't thrown");
    }
    catch (UnrecognizedOptionException e)
    {
        assertEquals("-unknown", e.getOption());
    }
}