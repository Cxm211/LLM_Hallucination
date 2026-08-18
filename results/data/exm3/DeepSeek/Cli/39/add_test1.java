// org/apache/commons/cli/PatternOptionBuilderTest.java
@Test
    public void testExistingFilePatternNotReadable() throws Exception {
        File tempFile = File.createTempFile("PatternOptionBuilderTest", ".tmp");
        try {
            if (tempFile.setReadable(false) && !tempFile.canRead()) {
                final Options options = PatternOptionBuilder.parsePattern("r<");
                final CommandLineParser parser = new PosixParser();
                final CommandLine line = parser.parse(options, new String[] { "-r", tempFile.getAbsolutePath() });
                assertNull("option r should be null for non-readable file", line.getOptionObject("r"));
            }
        } finally {
            tempFile.setReadable(true);
            tempFile.delete();
        }
    }
