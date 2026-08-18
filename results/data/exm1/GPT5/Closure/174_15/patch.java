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
        // Most passes try to report as many errors as possible,
        // so there may already be errors. We only care if there were
        // errors in the code we just parsed.
      // There was a parse error or IOException, so use a dummy block.
      root = IR.script();
      // Set the source name so that the compiler passes can track
      // the source file and module.
      root.setStaticSourceFile(sourceFile);
    } else {
      // Set the source name before preparing the AST so that all nodes
      // have the correct source information during preparation.
      root.setStaticSourceFile(sourceFile);
      compiler.prepareAst(root);
    }
  }
