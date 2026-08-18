// com/google/debugging/sourcemap/SourceMapGeneratorV3Test.java
public void testLineColumnAdjustment() throws Exception {
  checkSourceMap("var x = 1;\nvar y = 2;",
                 "{\n" +
                 "\"version\":3,\n" +
                 "\"file\":\"testcode\",\n" +
                 "\"lineCount\":1,\n" +
                 "\"mappings\":\"AAAAA,GAAI,CAAG,CACP,IAAIC,CAAE;\",\n" +
                 "\"sources\":[\"testcode\"],\n" +
                 "\"names\":[\"x\",\"y\"]\n" +
                 "}\n");
}