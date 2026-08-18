// org/apache/commons/cli/PosixParserTest.java
public void testUnrecognizedLongOption() throws Exception
    {
        Options options = new Options();
        String[] args = new String[] { "--unknown" };
        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("--unknown", e.getOption());
        }
    }
