// com/google/debugging/sourcemap/SourceMapGeneratorV3Test.java
public void testMultilineEdgeCase() throws Exception {
  compileAndCheck("function __FUNC__() {\nreturn __VAL__;\n}");
}