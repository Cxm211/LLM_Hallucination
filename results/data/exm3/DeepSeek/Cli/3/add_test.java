// org/apache/commons/cli/PatternOptionBuilderTest.java
public void testInvalidNumber() {
    try {
        Options options = PatternOptionBuilder.parsePattern("n");
        String[] args = new String[] { "-n", "invalid" };
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        line.getOptionObject('n');
        fail("Expected NumberFormatException for invalid number");
    } catch (NumberFormatException e) {
        // expected
    } catch (ParseException e) {
        fail("Unexpected ParseException: " + e.getMessage());
    }
}
