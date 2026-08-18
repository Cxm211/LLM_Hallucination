    private void annotateCalls(Node n) {
      Preconditions.checkState(n.isCall());

      // Keep track of of the "this" context of a call.  A call without an
      // explicit "this" is a free call.
      Node first = n.getFirstChild();

      // ignore cast nodes when determining if this is a free call
      Node target = first;
      while (target != null && target.isCast()) {
        target = target.getFirstChild();
      }

      if (!NodeUtil.isGet(target)) {
        n.putBooleanProp(Node.FREE_CALL, true);
      }

      // Keep track of the context in which eval is called. It is important
      // to distinguish between "(0, eval)()" and "eval()".
      if (first.isName() &&
          "eval".equals(first.getString())) {
        first.putBooleanProp(Node.DIRECT_EVAL, true);
      }
    }