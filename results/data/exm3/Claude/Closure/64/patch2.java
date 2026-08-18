  private String toSource(Node n, SourceMap sourceMap, boolean isFirstInput) {
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setPrettyPrint(options.prettyPrint);
    builder.setLineBreak(options.lineBreak);
    builder.setSourceMap(sourceMap);
    builder.setSourceMapDetailLevel(options.sourceMapDetailLevel);
    builder.setTagAsStrict(
        options.getLanguageOut() == LanguageMode.ECMASCRIPT5_STRICT && isFirstInput);
    builder.setLineLengthThreshold(options.lineLengthThreshold);

    Charset charset = options.outputCharset != null ?
        Charset.forName(options.outputCharset) : null;
    builder.setOutputCharset(charset);

    return builder.build();
  }