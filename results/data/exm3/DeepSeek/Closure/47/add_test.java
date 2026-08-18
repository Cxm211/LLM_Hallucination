// com/google/debugging/sourcemap/SourceMapGeneratorV3Test.java
public void testRoundTripLineColumn() throws Exception {
  String source = "a;";
  RunResult result = compile(source, "testcode");
  SourceMapConsumerV3 consumer = new SourceMapConsumerV3();
  consumer.parse(result.sourceMapFileContent);
  OriginalMapping mapping = consumer.getOriginalMappingFor(0, 0);
  assertNotNull(mapping);
  assertEquals(1, mapping.getLineNumber());
  assertEquals(1, mapping.getColumnPosition());
  assertEquals("a", mapping.getIdentifier());
}
