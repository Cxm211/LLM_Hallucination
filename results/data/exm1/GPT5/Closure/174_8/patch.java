  private void parse(AbstractCompiler compiler) {
    ParserRunner.ParseResult result = null;
    try {
      result = ParserRunner.parse(sourceFile, sourceFile.getCode(),
          compiler.getParserConfig(),
          compiler.getDefaultErrorReporter(),
          logger_);
      root = result.ast;
      compiler.setOldParseTree(sourceFile.getName(), result.oldAst);
    } catch (IOException e) {
      compiler.report(
          JSError.make(AbstractCompiler.READ_ERROR, sourceFile.getName()));
    }


    if (root == null) {
      // There was a parse error or IOException, so use a dummy block.
      root = IR.script();
    } else {
      compiler.prepareAst(root);
    }

    // Set the source name so that the compiler passes can track
    // the source file and module.
    root.setStaticSourceFile(sourceFile);
  }