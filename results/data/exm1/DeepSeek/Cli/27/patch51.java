// org.apache.commons.cli.HelpFormatterTest::testPrintHelpWithEmptySyntax
    public void testPrintHelpWithEmptySyntax()
    {
        HelpFormatter formatter = new HelpFormatter();
        try
        {
            formatter.printHelp(null, new Options());
            fail("null command line syntax should be rejected");
        }
        catch (IllegalArgumentException e)
        {
            
        }

        try
        {
            formatter.printHelp("", new Options());
            fail("empty command line syntax should be rejected");
        }
        catch (IllegalArgumentException e)
        {
            
        }
    }