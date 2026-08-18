// org/apache/commons/cli/PatternOptionBuilderTest.java
public void testNegativeNumber()
{
    try {
        Options options = PatternOptionBuilder.parsePattern("n%");
        String[] args = new String[] { "-n", "-3.14" };
        
        CommandLineParser parser = new PosixParser();
        CommandLine line = parser.parse(options, args);
        
        assertEquals("negative number flag n", new Double(-3.14), line.getOptionObject("n"));
        assertEquals("negative number flag n", new Double(-3.14), line.getOptionObject('n'));
    }
    catch (ParseException exp) {
        fail(exp.getMessage());
    }
}