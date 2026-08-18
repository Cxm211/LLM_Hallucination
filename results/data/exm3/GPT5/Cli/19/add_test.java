// org/apache/commons/cli/PosixParserTest.java::testUnrecognizedLongOption
public void testUnrecognizedLongOption() throws Exception
    {
        String[] args = new String[] { "--doesnotexist", "foo" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("--doesnotexist", e.getOption());
        }
    }