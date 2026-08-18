    private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && n.getType() == Token.GETPROP) {
        // We want to exclude the assignment itself from the usage list
        Node inner = n.getFirstChild();
        boolean isChainedProperty = inner != null && inner.getType() == Token.GETPROP;

        if (isChainedProperty) {
          Node child = inner.getLastChild();

          if (child != null && child.getType() == Token.STRING &&
              "prototype".equals(child.getString())) {
            return true;
          }
        }
      }

      return false;
    }
