// org/apache/commons/cli/ParserTestCase.java
public void testPropertyOptionFalseValue() throws Exception
{
    Options opts = new Options();
    opts.addOption("a", false, "option a");
    opts.addOption("b", false, "option b");
    
    String[] args = new String[] { };
    
    Properties properties = new Properties();
    properties.put("a", "false");
    properties.put("b", "no");
    
    CommandLine cmd = parse(parser, opts, args, properties);
    
    assertFalse(cmd.hasOption("a"));
    assertFalse(cmd.hasOption("b"));
}