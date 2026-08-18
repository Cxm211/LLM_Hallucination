// org/apache/commons/cli/bug/BugCLI252Test.java
@Test
public void testUniquePrefixMatch() throws ParseException {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("unique").build());
    options.addOption(Option.builder().longOpt("test").build());
    
    new DefaultParser().parse(options, new String[]{"--uni"});
}