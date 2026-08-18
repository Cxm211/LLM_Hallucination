// org/apache/commons/cli/OptionsTest.java
public void testMissingOptionsExceptionSingleOption() throws ParseException {
    Options options = new Options();
    options.addOption(OptionBuilder.isRequired().create("z"));
    try {
        new PosixParser().parse(options, new String[0]);
        fail("Expected MissingOptionException to be thrown");
    } catch (MissingOptionException e) {
        assertEquals("Missing required option: z", e.getMessage());
    }
}