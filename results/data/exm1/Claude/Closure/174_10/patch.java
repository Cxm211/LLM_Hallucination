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


  if (root == null) {
      root = IR.script();
  } else {
    compiler.prepareAst(root);
  }

  root.setStaticSourceFile(sourceFile);
}