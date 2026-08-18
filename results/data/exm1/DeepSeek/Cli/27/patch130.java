// org.apache.commons.cli.ParserTestCase::testReuseOptionsTwice
    public void testReuseOptionsTwice() throws Exception
    {
        Options opts = new Options();
		opts.addOption(OptionBuilder.isRequired().create('v'));

        
        parser.parse(opts, new String[] { "-v" });

        try
        {
            
            parser.parse(opts, new String[0]);
            fail("MissingOptionException not thrown");
        }
        catch (MissingOptionException e)
        {
            
        }
    }