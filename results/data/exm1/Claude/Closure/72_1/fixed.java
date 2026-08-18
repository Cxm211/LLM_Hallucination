// ===== FIXED com.google.javascript.jscomp.FunctionToBlockMutator :: makeLocalNamesUnique(Node, boolean) [lines 142-154] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-72-fixed/src/com/google/javascript/jscomp/FunctionToBlockMutator.java =====
  private void makeLocalNamesUnique(Node fnNode, boolean isCallInLoop) {
    Supplier<String> idSupplier = compiler.getUniqueNameIdSupplier();
    // Make variable names unique to this instance.
    NodeTraversal.traverse(
        compiler, fnNode, new MakeDeclaredNamesUnique(
            new InlineRenamer(
                idSupplier,
                "inline_",
                isCallInLoop)));
    // Make label names unique to this instance.
    new RenameLabels(compiler, new LabelNameSupplier(idSupplier), false)
        .process(null, fnNode);
  }
