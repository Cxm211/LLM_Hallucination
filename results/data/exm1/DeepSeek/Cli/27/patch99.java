// org.apache.commons.cli.ParserTestCase::testMissingArg
    public void testMissingArg() throws Exception
    {
        String[] args = new String[] { "-b" };

        boolean caught = false;

        try
        {
            parser.parse(options, args);
        }
        catch (MissingArgumentException e)
        {
            caught = true;
            assertEquals("option missing an argument", "b", e.getOption().getOpt());
        }

        assertTrue( "Confirm MissingArgumentException caught", caught );
    }