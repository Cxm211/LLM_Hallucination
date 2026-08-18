  private void parse(AbstractCompiler compiler) {
    try {
      ParserRunner.ParseResult result = ParserRunner.parse(sourceFile, sourceFile.getCode(),
          compiler.getParserConfig(),
          compiler.getDefaultErrorReporter(),
          logger_);
      root = result.ast;
      compiler.setOldParseTree(sourceFile.getName(), result.oldAst);
    } catch (IOException e) {
      compiler.report(
          JSError.make(AbstractCompiler.READ_ERROR, sourceFile.getName()));
    }

    if (root == null || compiler.hasHaltingErrors()) {
      root = IR.script();
    }

    // Set the source name so that the compiler passes can track
    // the source file and module.
    root.setStaticSourceFile(sourceFile);

    if (root != null && !compiler.hasHaltingErrors()) {
      compiler.prepareAst(root);
    }
  }