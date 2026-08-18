// org/apache/commons/cli/bug/BugCLI252Test.java
@Test
public void testPrefixMatchWithMultipleOptions() throws ParseException {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("test").build());
    options.addOption(Option.builder().longOpt("testing").build());
    options.addOption(Option.builder().longOpt("tester").build());
    
    try {
        new DefaultParser().parse(options, new String[]{"--test"});
    } catch (AmbiguousOptionException e) {
        fail("Should not throw AmbiguousOptionException for exact match when prefix matches exist");
    }
}