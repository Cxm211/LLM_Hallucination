// org/apache/commons/cli/OptionsTest.java
public void testMissingThreeOptionsException() throws ParseException {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().create("f"));
        options.addOption(OptionBuilder.isRequired().create("x"));
        options.addOption(OptionBuilder.isRequired().create("y"));
        try {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required options: fxy", e.getMessage());
        }
    }
