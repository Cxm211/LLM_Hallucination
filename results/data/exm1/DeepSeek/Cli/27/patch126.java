// org.apache.commons.cli.ParserTestCase::testMissingRequiredOptions
    public void testMissingRequiredOptions()
    {
        String[] args = new String[] { "-a" };

        Options options = new Options();
        options.addOption("a", "enable-a", false, null);
        options.addOption(OptionBuilder.withLongOpt("bfile").hasArg().isRequired().create('b'));
        options.addOption(OptionBuilder.withLongOpt("cfile").hasArg().isRequired().create('c'));

        try
        {
            parser.parse(options,args);
            fail("exception should have been thrown");
        }
        catch (MissingOptionException e)
        {
            assertEquals("Incorrect exception message", "Missing required options: b, c", e.getMessage());
            assertTrue(e.getMissingOptions().contains("b"));
            assertTrue(e.getMissingOptions().contains("c"));
        }
        catch (ParseException e)
        {
            fail("expected to catch MissingOptionException");
        }
    }