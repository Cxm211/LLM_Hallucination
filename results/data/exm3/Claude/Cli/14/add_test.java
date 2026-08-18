// org/apache/commons/cli2/bug/BugCLI144Test.java
public void testFileValidatorWithMissingFile() {
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

    final String fileName = "nonexistent-file.txt";
    try {
        CommandLine cl = parser
                .parseAndHelp(new String[] { "--file-name", fileName });
        fail("Expected OptionException for non-existent file");
    } catch (OptionException e) {
        // Expected behavior
    }
}