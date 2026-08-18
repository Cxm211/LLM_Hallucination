  private Node tryFoldSimpleFunctionCall(Node n) {
    Preconditions.checkState(n.isCall());
    Node callTarget = n.getFirstChild();
    if (callTarget != null && callTarget.isName() &&
          callTarget.getString().equals("String")) {
      // Fold String(a) to '' + (a) on immutable literals,
      // which allows further optimizations
      //
      // We can't do this in the general case, because String(a) has
      // slightly different semantics than '' + (a). See
      // http://code.google.com/p/closure-compiler/issues/detail?id=759
      Node value = callTarget.getNext();
      if (value != null) {
        // Check that there is exactly one argument.
        if (value.getNext() != null) {
          return n;
        }
        // Check that the argument is an immutable literal.
        if (!value.isNumber() && !value.isString() && !value.isTrue() && !value.isFalse() && !value.isNull()) {
          return n;
        }
        Node addition = IR.add(
            IR.string("").srcref(callTarget),
            value.detachFromParent());
        n.getParent().replaceChild(n, addition);
        reportCodeChange();
        return addition;
      }
    }
    return n;
  }