// org/apache/commons/cli/ParserTestCase.java
public void testPropertyOptionWithArg() throws Exception
{
    Options opts = new Options();
    opts.addOption("f", true, "option f with arg");
    
    String[] args = new String[] { };
    
    Properties properties = new Properties();
    properties.put("f", "value1");
    
    CommandLine cmd = parse(parser, opts, args, properties);
    
    assertTrue(cmd.hasOption("f"));
    assertEquals("value1", cmd.getOptionValue("f"));
}