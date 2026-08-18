// org/apache/commons/cli2/bug/BugLoopingOptionLookAlikeTest.java
public void testLoopingOptionLookAlike_ThreeValues() {
        final ArgumentBuilder abuilder = new ArgumentBuilder();
        final GroupBuilder gbuilder = new GroupBuilder();
        final Argument inputfile_opt = abuilder.withName("input").withMinimum(1).withMaximum(1).create();
        final Argument outputfile_opt = abuilder.withName("output").withMinimum(1).withMaximum(1).create();
        final Argument targets = new SourceDestArgument(inputfile_opt, outputfile_opt);
        final Group options = gbuilder.withOption(targets).create();
        final Parser parser = new Parser();
        parser.setGroup(options);
        try {
            parser.parse(new String[] { "file1.txt", "file2.txt", "file3.txt" });
            fail("OptionException expected");
        } catch (OptionException e) {
            assertEquals("Unexpected file3.txt while processing ", e.getMessage());
        }
    }