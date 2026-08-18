// org/apache/commons/cli/ParseRequiredTest.java
public void testReuseOptionsMultipleParsings() throws Exception
{
    Options opts = new Options();
    opts.addOption(OptionBuilder.isRequired().create('r'));
    opts.addOption(OptionBuilder.isRequired().create('s'));

    GnuParser parser = new GnuParser();

    // first parsing with all required options
    parser.parse(opts, new String[] { "-r", "-s" });

    // second parsing with all required options
    parser.parse(opts, new String[] { "-r", "-s" });

    try
    {
        // third parsing missing one required option
        parser.parse(opts, new String[] { "-r" });
        fail("MissingOptionException not thrown");
    }
    catch (MissingOptionException e)
    {
        // expected
    }
}