private String toSource(Node n, SourceMap sourceMap) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(options.prettyPrint);
    builder.setLineBreak(options.lineBreak);
    builder.setSourceMap(sourceMap);
    builder.setSourceMapDetailLevel(options.sourceMapDetailLevel);

    Charset charset = options.outputCharset;
    if (charset == null) {
      charset = Charsets.UTF_8;
    }
    builder.setOutputCharset(charset);

    return builder.build();
  }