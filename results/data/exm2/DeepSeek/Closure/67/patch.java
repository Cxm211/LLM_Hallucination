    private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && (n.getType() == Token.GETPROP || n.getType() == Token.GETELEM)
          ) {
        Node obj = n.getFirstChild();
        if (obj.getType() == Token.GETPROP) {
          Node prop = obj.getLastChild();
          if (prop.getType() == Token.STRING &&
              prop.getString().equals("prototype")) {
            return true;
          }
        }
      }

      return false;
    }