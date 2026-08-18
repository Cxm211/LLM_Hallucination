  private String toSource(Node n, SourceMap sourceMap) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);

    CompilerOptions effectiveOptions = this.options != null ? this.options
        : (compiler != null ? compiler.getOptions() : null);

    if (effectiveOptions != null) {
      builder.setPrettyPrint(effectiveOptions.prettyPrint);
      builder.setLineBreak(effectiveOptions.lineBreak);
      builder.setSourceMap(sourceMap);
      builder.setSourceMapDetailLevel(effectiveOptions.sourceMapDetailLevel);
      Charset charset = effectiveOptions.outputCharset;
      builder.setOutputCharset(charset);
    } else {
      builder.setSourceMap(sourceMap);
    }

    return builder.build();
  }