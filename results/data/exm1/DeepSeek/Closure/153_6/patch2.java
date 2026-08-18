  private void scanRoot(Node n, Scope parent) {
    if (n.getType() == Token.FUNCTION) {
      sourceName = (String) n.getProp(Node.SOURCENAME_PROP);

      final Node fnNameNode = n.getFirstChild();
      final Node args = fnNameNode.getNext();
      final Node body = args.getNext();

      String fnName = fnNameNode.getString();
      if (!fnName.isEmpty() && NodeUtil.isFunctionExpression(n)) {
        // Do not declare function expression name in outer scope.
      }

      Preconditions.checkState(args.getType() == Token.LP);
      for (Node a = args.getFirstChild(); a != null;
           a = a.getNext()) {
        Preconditions.checkState(a.getType() == Token.NAME);
        declareVar(a.getString(), a, args, n, null, n);
      }

      scanVars(body, n);
    } else {
      Preconditions.checkState(scope.getParent() == null);
      scanVars(n, null);
    }
  }