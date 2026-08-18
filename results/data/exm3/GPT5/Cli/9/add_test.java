// org/apache/commons/cli/OptionsTest.java::testMissingOptionsExceptionMultiple
public void testMissingOptionsExceptionMultiple() throws ParseException {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("a"));
        options.addOption(OptionBuilder.isRequired().create("b"));
        options.addOption(OptionBuilder.isRequired().create("c"));
        try {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required options: a, b, c", e.getMessage());
        }
    }