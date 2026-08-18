public void process(Node externs, Node root) {
    NodeTraversal.traverseRoots(compiler, externs, root, this);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    removeDuplicateDeclarations(root);
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }