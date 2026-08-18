public void enterScope(NodeTraversal t) {
      Node n = t.getCurrentNode();
      if (n != null && isCallToScopeMethod(n)) {
        transformation = transformationHandler.logAliasTransformation(
            n.getSourceFileName(), getSourceRegion(n));
      }
    }