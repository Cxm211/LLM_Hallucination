  private Node tryFoldSimpleFunctionCall(Node n) {
    Preconditions.checkState(n.isCall());
    Node callTarget = n.getFirstChild();
    if (callTarget != null && callTarget.isName() &&
          callTarget.getString().equals("String")) {
      // Fold String(a) to '' + (a) only when safe:
      // - exactly one argument
      // - the argument is an immutable literal
      Node value = callTarget.getNext();
      if (value != null && value.getNext() == null) {
        if (value.isString() || value.isNumber() || value.isTrue() ||
            value.isFalse() || value.isNull()) {
          Node addition = IR.add(
              IR.string("").srcref(callTarget),
              value.detachFromParent());
          addition.srcref(n);
          n.getParent().replaceChild(n, addition);
          reportCodeChange();
          return addition;
        }
      }
    }
    return n;
  }