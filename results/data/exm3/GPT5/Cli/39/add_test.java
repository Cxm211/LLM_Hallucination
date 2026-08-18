// org/apache/commons/cli/PatternOptionBuilderTest.java
@Test
    public void testExistingFilePatternWithDirectory() throws Exception {
        final Options options = PatternOptionBuilder.parsePattern("f<");
        final CommandLineParser parser = new PosixParser();
        final CommandLine line = parser.parse(options, new String[] { "-f", "src/test/resources" });

        assertNull("option f parsed for directory", line.getOptionObject("f"));
    }