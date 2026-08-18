private void traverseFunction(Node n, Node parent) {
    Preconditions.checkState(n.getChildCount() == 3);
    Preconditions.checkState(n.isFunction());

    final Node fnName = n.getFirstChild();

    boolean isFunctionExpression = (parent != null) && NodeUtil.isFunctionExpression(n);

    if (!isFunctionExpression) {
      // Functions declarations are in the scope containing the declaration.
      traverseBranch(fnName, n);
    }

    curNode = n;
    pushScope(n);

    // For function expressions, the name is only accessible within the function.
    if (isFunctionExpression) {
      // Don't traverse an empty name (anonymous functions).
      if (fnName.getChildCount() > 0 && !fnName.getString().isEmpty()) {
        traverseBranch(fnName, n);
      }
    }

    final Node args = fnName.getNext();
    final Node body = args.getNext();

    // Args
    traverseBranch(args, n);

    // Body
    Preconditions.checkState(body.getNext() == null && body.isBlock());
    traverseBranch(body, n);

    popScope();
  }