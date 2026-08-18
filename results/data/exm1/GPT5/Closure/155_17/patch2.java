  public void visit(NodeTraversal t, Node n, Node parent) {
    if (n.getType() == Token.NAME) {
      Var v = t.getScope().getVar(n.getString());
      if (v != null && varFilter.apply(v)) {
        addReference(t, v,
            new Reference(n, parent, t, blockStack.peek()));
      }
    }

    // Popping the block stack should occur on exit, not during pre-order visit.
  }