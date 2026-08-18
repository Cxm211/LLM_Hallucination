// org.apache.commons.cli.ParserTestCase::testAmbiguousPartialLongOption3
    public void testAmbiguousPartialLongOption3() throws Exception
    {
        String[] args = new String[] { "--ver=1" };
        
        Options options = new Options();
        options.addOption(OptionBuilder.withLongOpt("version").create());
        options.addOption(OptionBuilder.withLongOpt("verbose").hasOptionalArg().create());
        
        boolean caught = false;
        
        try 
        {
            parser.parse(options, args);
        }
        catch (AmbiguousOptionException e) 
        {
            caught = true;
            assertEquals("Partial option", "--ver", e.getOption());
            assertNotNull("Matching options null", e.getMatchingOptions());
            assertEquals("Matching options size", 2, e.getMatchingOptions().size());
        }
        
        assertTrue( "Confirm MissingArgumentException caught", caught );
    }