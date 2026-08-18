// org/apache/commons/cli/bug/BugCLI252Test.java
@Test
public void testAmbiguousPrefixMatch() {
    Options options = new Options();
    options.addOption(Option.builder().longOpt("verbose").build());
    options.addOption(Option.builder().longOpt("version").build());
    
    try {
        new DefaultParser().parse(options, new String[]{"--ver"});
        fail("Should throw AmbiguousOptionException for ambiguous prefix");
    } catch (AmbiguousOptionException e) {
        // Expected
    } catch (ParseException e) {
        fail("Wrong exception type");
    }
}