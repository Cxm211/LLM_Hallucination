// org/apache/commons/cli2/bug/BugCLI144Test.java
public void testRequiredOptionSingleValidation() {
    final DefaultOptionBuilder obuilder = new DefaultOptionBuilder();
    final ArgumentBuilder abuilder = new ArgumentBuilder();
    Validator validator = new Validator() {
        private int count = 0;
        public void validate(final WriteableCommandLine commandLine, final Option option,
                             final List values) throws OptionException {
            count++;
            if (count > 1) {
                throw new OptionException(option, "Validated more than once");
            }
        }
    };
    DefaultOption option = obuilder.withShortName("o")
            .withLongName("option").withRequired(true)
            .withArgument(abuilder.withName("value").withValidator(validator).create())
            .create();
    Group group = new GroupBuilder().withName("group").withOption(option).create();
    Parser parser = new Parser();
    parser.setGroup(group);
    CommandLine cl = parser.parseAndHelp(new String[] { "--option", "value" });
    assertNotNull(cl);
}
