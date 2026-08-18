// org/apache/commons/cli/PatternOptionBuilderTest.java
public void testIntegerNumber()
{
    try {
        Options options = PatternOptionBuilder.parsePattern("n%");
        String[] args = new String[] { "-n", "42" };
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        
        Number result = (Number) line.getOptionObject("n");
        assertNotNull("number should not be null", result);
        assertEquals("integer number value", 42, result.intValue());
    }
    catch (ParseException exp) {
        fail(exp.getMessage());
    }
}