// org/apache/commons/cli/PatternOptionBuilderTest.java
@Test
public void testFilePatternCreatesFile() throws Exception {
    final Options options = PatternOptionBuilder.parsePattern("f>");
    final CommandLineParser parser = new PosixParser();
    final CommandLine line = parser.parse(options, new String[] { "-f", "any-file-path.txt" });
    final Object parsedFile = line.getOptionObject("f");
    assertNotNull("option f not parsed", parsedFile);
    assertTrue("option f should be File", parsedFile instanceof File);
    assertEquals("any-file-path.txt", ((File) parsedFile).getName());
}