public void process(Node externs, Node root) {
    NodeTraversal t = new NodeTraversal(
        compiler, new NormalizeStatements(compiler, assertOnChange));
    t.traverseRoots(externs, root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t2 = new NodeTraversal(compiler, renamer);
      t2.traverseRoots(externs, root);
    }
    removeDuplicateDeclarations(externs, root);
    new PropagateConstantAnnotationsOverVars(compiler, assertOnChange)
        .process(externs, root);

    if (!compiler.getLifeCycleStage().isNormalized()) {
      compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    }
  }