// org/apache/commons/cli/ParserTestCase.java
public void testPropertyOptionGroupConflict() throws Exception
    {
        Options opts = new Options();
        
        OptionGroup group = new OptionGroup();
        group.addOption(new Option("x", null));
        group.addOption(new Option("y", null));
        opts.addOptionGroup(group);
        
        Properties properties = new Properties();
        properties.put("x", "true");
        properties.put("y", "true");
        
        try {
            parse(parser, opts, null, properties);
            fail("AlreadySelectedException expected");
        } catch (AlreadySelectedException e) {
            // expected
        }
    }