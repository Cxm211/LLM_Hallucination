// org.apache.commons.cli.ParserTestCase::testMissingArgWithBursting
    public void testMissingArgWithBursting() throws Exception
    {
        String[] args = new String[] { "-acb" };

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