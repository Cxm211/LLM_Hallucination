// org/apache/commons/cli/PatternOptionBuilderTest.java
@Test
    public void testExistingFilePatternDirectory() throws Exception {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "PatternOptionBuilderTest-" + System.currentTimeMillis());
        tempDir.mkdirs();
        try {
            final Options options = PatternOptionBuilder.parsePattern("d<");
            final CommandLineParser parser = new PosixParser();
            final CommandLine line = parser.parse(options, new String[] { "-d", tempDir.getAbsolutePath() });
            assertNull("option d should be null for directory", line.getOptionObject("d"));
        } finally {
            tempDir.delete();
        }
    }
