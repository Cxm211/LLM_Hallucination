private Node tryFoldSimpleFunctionCall(Node n) {
    Preconditions.checkState(n.isCall());
    Node callTarget = n.getFirstChild();
    if (callTarget != null && callTarget.isName() &&
          callTarget.getString().equals("String")) {
      Node value = callTarget.getNext();
      if (value != null && value.getNext() == null) {
        if (NodeUtil.isImmutableValue(value)) {
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