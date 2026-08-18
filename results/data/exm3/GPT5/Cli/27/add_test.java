// org/apache/commons/cli/ParserTestCase.java::testOptionGroupLongTwoOptions
public void testOptionGroupLongTwoOptions() throws Exception
    {
        OptionGroup group = new OptionGroup();
        group.addOption(OptionBuilder.withLongOpt("foo").create());
        group.addOption(OptionBuilder.withLongOpt("bar").create());
        
        Options options = new Options();
        options.addOptionGroup(group);
        
        try {
            parser.parse(options, new String[] { "--foo", "--bar" });
            fail("Expected AlreadySelectedException");
        } catch (AlreadySelectedException e) {
            // expected
        }
    }