// org/apache/commons/cli2/bug/BugCLI158Test.java
public void testNoArgumentsAllDefaults() throws Exception {
    String[] args = new String[]{};
    final ArgumentBuilder abuilder = new ArgumentBuilder();
    final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    final GroupBuilder gbuilder = new GroupBuilder();

    DefaultOption bOption = obuilder.withShortName("b")
            .withLongName("b")
            .withArgument(abuilder.withName("b")
                    .withMinimum(0)
                    .withMaximum(3)
                    .withDefault("100")
                    .withDefault("1000")
                    .withDefault("10000")
                    .create())
            .create();

    Group options = gbuilder
            .withName("options")
            .withOption(bOption)
            .create();

    Parser parser = new Parser();
    parser.setHelpTrigger("--help");
    parser.setGroup(options);
    CommandLine cl = parser.parse(args);
    CommandLine cmd = cl;
    assertNotNull(cmd);
    List b = cmd.getValues("-b");
    assertEquals("[100, 1000, 10000]", b + "");
}