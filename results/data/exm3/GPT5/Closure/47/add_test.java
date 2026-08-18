// com/google/debugging/sourcemap/SourceMapGeneratorV3Test.java::testOneBasedOriginalPositions
public void testOneBasedOriginalPositions() throws Exception {
    RunResult result = compile("var __FOO__ = 1;", "in.js");
    SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
    consumer.parse(result.sourceMapFileContent);
    OriginalMapping m = consumer.getMappingForLine(1, 1);
    assertNotNull(m);
    assertEquals("in.js", m.getOriginalFile());
    assertEquals(1, m.getLineNumber());
    assertEquals(1, m.getColumnPosition());
  }