// org/apache/commons/cli2/bug/BugCLI144Test.java
public void testOptionalGroupWithRequiredChildNotPresent() {
    final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    final ArgumentBuilder abuilder = new ArgumentBuilder();
    final GroupBuilder gbuilder = new GroupBuilder();
    DefaultOption requiredOption = obuilder.withShortName("r")
            .withLongName("required").withRequired(true)
            .withArgument(abuilder.withName("required").create())
            .create();
    Group optionalGroup = gbuilder.withName("group").withOption(requiredOption).create();
    Parser parser = new Parser();
    parser.setGroup(optionalGroup);
    CommandLine cl = parser.parseAndHelp(new String[] {});
    assertNotNull(cl);
}
