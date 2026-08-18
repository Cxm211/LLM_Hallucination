private void annotateCalls(Node n) {
  Preconditions.checkState(n.isCall());

  Node first = n.getFirstChild();

  if (!NodeUtil.isGet(first)) {
    n.putBooleanProp(Node.FREE_CALL, true);
  }

  if (first.isName() &&
      "eval".equals(first.getString()) &&
      !NodeUtil.isGet(first)) {
    first.putBooleanProp(Node.DIRECT_EVAL, true);
  }
}