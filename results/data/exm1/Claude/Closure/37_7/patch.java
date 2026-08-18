private void traverseFunction(Node n, Node parent) {
    Preconditions.checkState(n.getChildCount() == 3);
    Preconditions.checkState(n.isFunction());

    final Node fnName = n.getFirstChild();

    boolean isFunctionExpression = (parent != null)
        && NodeUtil.isFunctionExpression(n);

    if (!isFunctionExpression) {
      traverseBranch(fnName, n);
    }

    curNode = n;
    pushScope(n);

    if (isFunctionExpression && !fnName.getString().isEmpty()) {
      traverseBranch(fnName, n);
    }

    final Node args = fnName.getNext();
    final Node body = args.getNext();

    traverseBranch(args, n);

    Preconditions.checkState(body.getNext() == null &&
            body.isBlock());
    traverseBranch(body, n);

    popScope();
  }