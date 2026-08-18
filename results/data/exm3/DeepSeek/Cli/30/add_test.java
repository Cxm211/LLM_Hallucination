// org/apache/commons/cli/ParserTestCase.java
public void testPropertyOptionGroupWithArgument() throws Exception
{
    Options opts = new Options();
    
    OptionGroup group = new OptionGroup();
    group.addOption(new Option("a", false, "option a"));
    group.addOption(new Option("b", true, "option b with argument"));
    opts.addOptionGroup(group);
    
    String[] args = new String[] { "-a" };
    
    Properties properties = new Properties();
    properties.put("b", "someValue");
    
    CommandLine cmd = parse(parser, opts, args, properties);
    
    assertTrue(cmd.hasOption("a"));
    assertFalse(cmd.hasOption("b"));
}
