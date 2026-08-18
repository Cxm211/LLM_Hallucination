// org/apache/commons/cli2/bug/BugLoopingOptionLookAlikeTest.java
public void testLoopingOptionLookAlike_SingleValue() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Argument inputfile_opt = abuilder.withName("input").withMinimum(1).withMaximum(1).create();
        final Argument outputfile_opt = abuilder.withName("output").withMinimum(1).withMaximum(1).create();
        final Argument targets = new SourceDestArgument(inputfile_opt, outputfile_opt);
        final Group options = gbuilder.withOption(targets).create();
        final Parser parser = new Parser();
        parser.setGroup(options);
        try {
            CommandLine cl = parser.parse(new String[] { "input.txt", "output.txt" });
            assertNotNull(cl);
        } catch (OptionException e) {
            fail("Should not throw OptionException for valid input: " + e.getMessage());
        }
    }