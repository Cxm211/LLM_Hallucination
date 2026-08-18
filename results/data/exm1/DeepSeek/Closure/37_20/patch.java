private void traverseFunction(Node n, Node parent) {
    Preconditions.checkState(n.getChildCount() == 3);
    Preconditions.checkState(n.isFunction());

    final Node fnName = n.getFirstChild();

    boolean isFunctionExpression = (parent != null)
        && NodeUtil.isFunctionExpression(n);

    if (isFunctionExpression) {
      curNode = n;
      pushScope(n);
      traverseBranch(fnName, n);
    } else {
      traverseBranch(fnName, n);
      curNode = n;
      pushScope(n);
    }

    final Node args = fnName.getNext();
    final Node body = args.getNext();

    traverseBranch(args, n);
    Preconditions.checkState(body.getNext() == null &&
            body.isBlock());
    traverseBranch(body, n);

    popScope();
  }