// buggy function
    protected void setOptions(final Options options) {
        this.options = options;
        this.requiredOptions = options.getRequiredOptions();
    }

// trigger testcase
// org/apache/commons/cli/ParseRequiredTest.java::testReuseOptionsTwice
public void testReuseOptionsTwice() throws Exception
    {
        Options opts = new Options();
		opts.addOption(OptionBuilder.isRequired().create('v'));

		GnuParser parser = new GnuParser();

        // first parsing
        parser.parse(opts, new String[] { "-v" });

        try
        {
            // second parsing, with the same Options instance and an invalid command line
            parser.parse(opts, new String[0]);
            fail("MissingOptionException not thrown");
        }
        catch (MissingOptionException e)
        {
            // expected
        }
    }
