// org/apache/commons/cli2/bug/BugCLI158Test.java
public void testSingleOptionOneArgumentThreeDefaults() throws Exception {
    String[] args = new String[]{"-c", "1"};
    final ArgumentBuilder abuilder = new ArgumentBuilder();
    final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    final GroupBuilder gbuilder = new GroupBuilder();

    DefaultOption cOption = obuilder.withShortName("c")
            .withLongName("c")
            .withArgument(abuilder.withName("c")
                    .withMinimum(1)
                    .withMaximum(3)
                    .withDefault("100")
                    .withDefault("200")
                    .withDefault("300")
                    .create())
            .create();

    Group options = gbuilder
            .withName("options")
            .withOption(cOption)
            .create();

    Parser parser = new Parser();
    parser.setHelpTrigger("--help");
    parser.setGroup(options);
    CommandLine cl = parser.parse(args);
    CommandLine cmd = cl;
    assertNotNull(cmd);
    List c = cmd.getValues("-c");
    assertEquals("[1, 200, 300]", c + "");
}
