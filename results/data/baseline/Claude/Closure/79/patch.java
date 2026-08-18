public void process(Node externs, Node root) {
    NodeTraversal t1 = new NodeTraversal(
        compiler, new NormalizeStatements(compiler, assertOnChange));
    t1.traverseRoots(externs, root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    removeDuplicateDeclarations(externs, root);
    new PropagateConstantAnnotationsOverVars(compiler, assertOnChange)
        .process(externs, root);

    if (!compiler.getLifeCycleStage().isNormalized()) {
      compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    }
  }