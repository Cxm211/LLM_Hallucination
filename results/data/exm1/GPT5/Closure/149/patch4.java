  private String toSource(Node n, SourceMap sourceMap) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    CompilerOptions opts = compiler != null ? compiler.getOptions() : null;
    if (opts != null) {
      builder.setPrettyPrint(opts.prettyPrint);
      builder.setLineBreak(opts.lineBreak);
      builder.setSourceMap(sourceMap);
      builder.setSourceMapDetailLevel(opts.sourceMapDetailLevel);
      Charset charset = opts.outputCharset;
      builder.setOutputCharset(charset);
    } else {
      builder.setSourceMap(sourceMap);
    }
    return builder.build();
  }