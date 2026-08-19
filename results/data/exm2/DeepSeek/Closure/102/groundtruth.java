// ===== FIXED com.google.javascript.jscomp.Normalize :: process(Node, Node) [lines 87-97] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-102-fixed/src/com/google/javascript/jscomp/Normalize.java =====
  public void process(Node externs, Node root) {
    NodeTraversal.traverse(compiler, root, this);
    removeDuplicateDeclarations(root);
    if (MAKE_LOCAL_NAMES_UNIQUE) {
      MakeDeclaredNamesUnique renamer = new MakeDeclaredNamesUnique();
      NodeTraversal t = new NodeTraversal(compiler, renamer);
      t.traverseRoots(externs, root);
    }
    new PropogateConstantAnnotations(compiler, assertOnChange)
        .process(externs, root);
  }
