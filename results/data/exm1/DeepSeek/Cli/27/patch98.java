// org.apache.commons.cli.ParserTestCase::testUnrecognizedOption
    public void testUnrecognizedOption() throws Exception
    {
        String[] args = new String[] { "-a", "-d", "-b", "toast", "foo", "bar" };

        try
        {
            parser.parse(options, args);
            fail("UnrecognizedOptionException wasn't thrown");
        }
        catch (UnrecognizedOptionException e)
        {
            assertEquals("-d", e.getOption());
        }
    }