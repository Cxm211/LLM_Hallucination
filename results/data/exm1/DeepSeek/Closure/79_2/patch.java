public void process(Node externs, Node root) {
    new NodeTraversal(
        compiler, new NormalizeStatements(compiler, assertOnChange))
        .traverse(root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    // Propagate constant annotations before removing duplicate declarations
    new PropagateConstantAnnotationsOverVars(compiler, assertOnChange)
        .process(externs, root);
    removeDuplicateDeclarations(externs, root);
    if (!compiler.getLifeCycleStage().isNormalized()) {
      compiler.setLifeCycleStage(LifeCycleStage.NORMALIZED);
    }
  }