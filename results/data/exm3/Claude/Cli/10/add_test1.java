// org/apache/commons/cli/ParseRequiredTest.java
public void testReuseOptionsAfterSuccessfulParse() throws Exception
{
    Options opts = new Options();
    opts.addOption(OptionBuilder.isRequired().create('m'));

    GnuParser parser = new GnuParser();

    // first parsing succeeds
    parser.parse(opts, new String[] { "-m" });

    // second parsing also succeeds
    parser.parse(opts, new String[] { "-m" });

    // third parsing also succeeds
    parser.parse(opts, new String[] { "-m" });
}