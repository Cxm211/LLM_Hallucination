// org/apache/commons/cli/PatternOptionBuilderTest.java
@Test
public void testExistingFilePatternUnreadableFile() throws Exception {
    final Options options = PatternOptionBuilder.parsePattern("u<");
    final CommandLineParser parser = new PosixParser();
    File tempFile = File.createTempFile("test-unreadable", ".tmp");
    tempFile.setReadable(false);
    try {
        final CommandLine line = parser.parse(options, new String[] { "-u", tempFile.getAbsolutePath() });
        assertNull("option u should not be parsed for unreadable file", line.getOptionObject("u"));
    } finally {
        tempFile.setReadable(true);
        tempFile.delete();
    }
}