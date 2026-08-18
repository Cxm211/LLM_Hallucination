public void process(Node externs, Node root) {
    NodeTraversal t0 = new NodeTraversal(compiler, this);
    t0.traverseRoots(externs, root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    removeDuplicateDeclarations(root);
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }