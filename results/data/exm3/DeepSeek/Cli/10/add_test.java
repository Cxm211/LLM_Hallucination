// org/apache/commons/cli/ParseRequiredTest.java
public void testReuseOptionsWithAddedRequiredOption() throws Exception {
    Options opts = new Options();
    opts.addOption(OptionBuilder.isRequired().create('a'));
    GnuParser parser = new GnuParser();
    parser.parse(opts, new String[] { "-a" });
    opts.addOption(OptionBuilder.isRequired().create('b'));
    try {
        parser.parse(opts, new String[] { "-b" });
        fail("MissingOptionException not thrown");
    } catch (MissingOptionException e) {
        // expected
    }
}
