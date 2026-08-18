    private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && n.isGetProp()
          && assign.getParent().isExprResult()) {
        boolean isChainedProperty =
            n.getFirstChild().isGetProp();

        if (isChainedProperty) {
          Node child = n.getFirstChild().getFirstChild().getNext();

          if (child.isString() &&
              child.getString().equals("prototype")) {
            return true;
          }
        } else if (n.getLastChild().isString() &&
            n.getLastChild().getString().equals("prototype")) {
          return true;
        }
      }

      return false;
    }