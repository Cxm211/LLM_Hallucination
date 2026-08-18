private void annotateCalls(Node n) {
  Preconditions.checkState(n.isCall());

  // Keep track of the "this" context of a call. A call without an
  // explicit "this" is a free call.
  Node first = n.getFirstChild();

  // ignore cast nodes by unwrapping them.
  Node target = first;
  while (target != null && target.isCast()) {
    target = target.getFirstChild();
  }
  if (target == null) {
    target = first;
  }

  if (!NodeUtil.isGet(target)) {
    n.putBooleanProp(Node.FREE_CALL, true);
  }

  // Keep track of the context in which eval is called. It is important
  // to distinguish between "(0, eval)()" and "eval()".
  if (target.isName() && "eval".equals(target.getString())) {
    target.putBooleanProp(Node.DIRECT_EVAL, true);
  }
}
