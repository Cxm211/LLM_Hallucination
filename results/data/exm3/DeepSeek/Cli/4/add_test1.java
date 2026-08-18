// org/apache/commons/cli/OptionsTest.java
public void testMissingLongOptionException() throws ParseException {
        Options options = new Options();
        options.addOption(OptionBuilder.isRequired().withLongOpt("file").create());
        try {
            new PosixParser().parse(options, new String[0]);
            fail("Expected MissingOptionException to be thrown");
        } catch (MissingOptionException e) {
            assertEquals("Missing required option: file", e.getMessage());
        }
    }
