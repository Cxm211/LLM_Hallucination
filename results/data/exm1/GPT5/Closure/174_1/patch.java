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
      // There was a parse error or IOException, so use a dummy block.
      root = IR.script();
    }

    // Set the source name so that the compiler passes can track
    // the source file and module. Do this before preparing the AST.
    root.setStaticSourceFile(sourceFile);

    if (!(compiler.hasHaltingErrors())) {
      // Only prepare the AST if we didn't have halting errors.
      // (If there were halting errors, 'root' is a dummy script.)
      if (root.hasChildren() || root != null) {
        compiler.prepareAst(root);
      }
    }
  }