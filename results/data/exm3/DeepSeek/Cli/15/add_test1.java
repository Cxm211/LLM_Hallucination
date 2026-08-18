// org/apache/commons/cli2/bug/BugCLI158Test.java
public void testSingleOptionTwoArgumentsFourDefaults() throws Exception {
    String[] args = new String[]{"-d", "1", "2"};
    final ArgumentBuilder abuilder = new ArgumentBuilder();
    final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    final GroupBuilder gbuilder = new GroupBuilder();

    DefaultOption dOption = obuilder.withShortName("d")
            .withLongName("d")
            .withArgument(abuilder.withName("d")
                    .withMinimum(2)
                    .withMaximum(4)
                    .withDefault("100")
                    .withDefault("200")
                    .withDefault("300")
                    .withDefault("400")
                    .create())
            .create();

    Group options = gbuilder
            .withName("options")
            .withOption(dOption)
            .create();

    Parser parser = new Parser();
    parser.setHelpTrigger("--help");
    parser.setGroup(options);
    CommandLine cl = parser.parse(args);
    CommandLine cmd = cl;
    assertNotNull(cmd);
    List d = cmd.getValues("-d");
    assertEquals("[1, 2, 300, 400]", d + "");
}
