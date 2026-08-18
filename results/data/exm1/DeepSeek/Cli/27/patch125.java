// org.apache.commons.cli.ParserTestCase::testMissingRequiredOption
    public void testMissingRequiredOption()
    {
        String[] args = new String[] { "-a" };
        
        Options options = new Options();
        options.addOption("a", "enable-a", false, null);
        options.addOption(OptionBuilder.withLongOpt("bfile").hasArg().isRequired().create('b'));

        try
        {
            parser.parse(options,args);
            fail("exception should have been thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals( "Incorrect exception message", "Missing required option: b", e.getMessage() );
            assertTrue(e.getMissingOptions().contains("b"));
        }
        catch (ParseException e)
        {
            fail("expected to catch MissingOptionException");
        }
    }