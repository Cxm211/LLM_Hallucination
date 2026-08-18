private Node tryFoldSimpleFunctionCall(Node n) {
    Preconditions.checkState(n.isCall());
    Node callTarget = n.getFirstChild();
    if (callTarget != null && callTarget.isName() &&
          callTarget.getString().equals("String")) {
      // Fold String(a) to '' + (a) only when there is exactly one argument
      // and it is an immutable literal. This avoids changing semantics for
      // objects or multiple-argument calls.
      Node value = callTarget.getNext();
      // Ensure exactly one argument
      if (value != null && value.getNext() == null) {
        // Check for immutable literal: string, number, boolean, or null
        boolean isImmutable = value.isString() || value.isNumber() ||
            value.isTrue() || value.isFalse() || value.isNull();
        if (isImmutable) {
          Node addition = IR.add(
              IR.string("").srcref(callTarget),
              value.detachFromParent());
          n.getParent().replaceChild(n, addition);
          reportCodeChange();
          return addition;
        }
      }
    }
    return n;
  }