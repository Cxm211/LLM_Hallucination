// org/apache/commons/cli/PatternOptionBuilderTest.java
public void testInvalidNumberPattern()
{
    Options options = PatternOptionBuilder.parsePattern("n%");
    String[] args = new String[] { "-n", "not_a_number" };

    CommandLineParser parser = new PosixParser();
    try {
        parser.parse(options, args);
        fail("Expected ParseException");
    } catch (ParseException expected) {
        // expected
    }
}