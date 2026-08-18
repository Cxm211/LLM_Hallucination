// org/apache/commons/cli/PatternOptionBuilderTest.java
public void testNumberFormatException()
{
    try {
        Options options = PatternOptionBuilder.parsePattern("n%");
        String[] args = new String[] { "-n", "invalid_number" };
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        
        // This should throw NumberFormatException because "invalid_number" is not a valid number
        Object result = line.getOptionObject("n");
        fail("Expected NumberFormatException to be thrown");
    }
    catch (ParseException exp) {
        // Expected behavior: ParseException wrapping NumberFormatException
        assertTrue("Expected ParseException with NumberFormatException cause", 
                   exp.getCause() instanceof NumberFormatException || 
                   exp.getMessage().contains("number"));
    }
}