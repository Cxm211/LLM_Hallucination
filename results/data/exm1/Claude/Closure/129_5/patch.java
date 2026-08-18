private void annotateCalls(Node n) {
  Preconditions.checkState(n.isCall());

  // Keep track of of the "this" context of a call.  A call without an
  // explicit "this" is a free call.
  Node first = n.getFirstChild();

  // ignore cast nodes.

  if (!NodeUtil.isGet(first)) {
    n.putBooleanProp(Node.FREE_CALL, true);
  }

  // Keep track of the context in which eval is called. It is important
  // to distinguish between "(0, eval)()" and "eval()".
  if (first.isName() &&
      "eval".equals(first.getString())) {
    first.putBooleanProp(Node.DIRECT_EVAL, true);
  } else if (NodeUtil.isGet(first)) {
    // Check if it's a method call like foo.call(this, ...)
    Node callTarget = first.getFirstChild();
    if (callTarget != null && callTarget.isGetProp()) {
      Node getPropTarget = callTarget.getFirstChild();
      String propName = callTarget.getLastChild().getString();
      // For patterns like "foo.call" where foo could be anything,
      // if the property is 'call' or 'apply', this is not a free call
      if ("call".equals(propName) || "apply".equals(propName)) {
        n.putBooleanProp(Node.FREE_CALL, false);
      }
    }
  }
}