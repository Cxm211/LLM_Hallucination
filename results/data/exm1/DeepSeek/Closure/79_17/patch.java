public void process(Node externs, Node root) {
    new NodeTraversal(
        compiler, new NormalizeStatements(compiler, assertOnChange))
        .traverse(root);
    // Always run MakeDeclaredNamesUnique to ensure catch block exception names
    // are handled before removeDuplicateDeclarations.
    MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
    NodeTraversal t = new NodeTraversal(compiler, renamer);
    t.traverseRoots(externs, root);
    removeDuplicateDeclarations(externs, root);
    new PropagateConstantAnnotationsOverVars(compiler, assertOnChange)
        .process(externs, root);

    if (!compiler.getLifeCycleStage().isNormalized()) {
      compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    }
  }