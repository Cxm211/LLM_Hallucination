// org.apache.commons.cli.ParserTestCase::testUnrecognizedOptionWithBursting
    public void testUnrecognizedOptionWithBursting() throws Exception
    {
        String[] args = new String[] { "-adbtoast", "foo", "bar" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-adbtoast", e.getOption());
        }
    }