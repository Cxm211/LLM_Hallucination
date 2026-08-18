private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && (n.getType() == Token.GETPROP || n.getType() == Token.GETELEM)) {
        // We want to exclude the assignment itself from the usage list
        Node base = n.getFirstChild();
        if (base != null && (base.getType() == Token.GETPROP || base.getType() == Token.GETELEM)) {
          Node nameNode = base.getFirstChild() != null ? base.getFirstChild().getNext() : null;
          if (nameNode != null && nameNode.getType() == Token.STRING &&
              "prototype".equals(nameNode.getString())) {
            return true;
          }
        }
      }

      return false;
    }