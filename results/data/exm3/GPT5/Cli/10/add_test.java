// org/apache/commons/cli/ParseRequiredTest.java::testReuseOptionsAcrossParsers
public void testReuseOptionsAcrossParsers() throws Exception
    {
        Options opts = new Options();
        opts.addOption(OptionBuilder.isRequired().create('v'));

        GnuParser p1 = new GnuParser();
        // first parsing with valid command line
        p1.parse(opts, new String[] { "-v" });

        GnuParser p2 = new GnuParser();
        try
        {
            // second parsing using a different parser instance with the same Options and an invalid command line
            p2.parse(opts, new String[0]);
            fail("MissingOptionException not thrown");
        }
        catch (MissingOptionException e)
        {
            // expected
        }
    }