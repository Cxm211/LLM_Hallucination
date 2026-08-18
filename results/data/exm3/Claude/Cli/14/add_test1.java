// org/apache/commons/cli2/bug/BugCLI144Test.java
public void testFileValidatorWithMissingRequiredOption() {
    final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    final ArgumentBuilder abuilder = new ArgumentBuilder();
    final GroupBuilder gbuilder = new GroupBuilder();
    DefaultOption fileNameOption = obuilder.withShortName("f")
            .withLongName("file-name").withRequired(true).withDescription(
                    "name of an existing file").withArgument(
                    abuilder.withName("file-name").withValidator(
                            FileValidator.getExistingFileInstance())
                            .create()).create();
    Group options = gbuilder.withName("options").withOption(fileNameOption)
            .create();
    Parser parser = new Parser();
    parser.setHelpTrigger("--help");
    parser.setGroup(options);

    try {
        CommandLine cl = parser.parseAndHelp(new String[] {});
        fail("Expected OptionException for missing required option");
    } catch (OptionException e) {
        // Expected behavior
    }
}