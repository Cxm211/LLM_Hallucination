  private void traverseFunction(Node n, Node parent) {
    Preconditions.checkState(n.getChildCount() == 3);
    Preconditions.checkState(n.isFunction());

    final Node fnName = n.getFirstChild();
    final boolean isFnNameEmpty = fnName.getString().isEmpty();

    boolean isFunctionExpression = (parent != null)
        && NodeUtil.isFunctionExpression(n);

    if (!isFunctionExpression && !isFnNameEmpty) {
      traverseBranch(fnName, n);
    }

    curNode = n;
    pushScope(n);

    if (isFunctionExpression && !isFnNameEmpty) {
      traverseBranch(fnName, n);
    }

    final Node args = fnName.getNext();
    final Node body = args.getNext();

    // Args
    traverseBranch(args, n);

    // Body
    Preconditions.checkState(body.getNext() == null &&
            body.isBlock());
    traverseBranch(body, n);

    popScope();
  }