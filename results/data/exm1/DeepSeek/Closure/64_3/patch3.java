  private String toSource(Node n, SourceMap sourceMap, boolean addStrictDirective) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(options.prettyPrint);
    builder.setLineBreak(options.lineBreak);
    builder.setSourceMap(sourceMap);
    builder.setSourceMapDetailLevel(options.sourceMapDetailLevel);
    builder.setTagAsStrict(
        addStrictDirective && options.getLanguageOut() == LanguageMode.ECMASCRIPT5_STRICT);
    builder.setLineLengthThreshold(options.lineLengthThreshold);

    Charset charset = options.outputCharset != null ?
        Charset.forName(options.outputCharset) : null;
    builder.setOutputCharset(charset);

    return builder.build();
  }