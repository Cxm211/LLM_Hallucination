    public void enterScope(NodeTraversal t) {
      Node current = t.getCurrentNode();
      if (current != null) {
        Node n = current.getParent();
        if (n != null && isCallToScopeMethod(n)) {
          transformation = transformationHandler.logAliasTransformation(
              n.getSourceFileName(), getSourceRegion(n));
        }
      }
    }