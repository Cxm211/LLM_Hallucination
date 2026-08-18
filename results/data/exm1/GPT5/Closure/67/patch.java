private boolean isPrototypePropertyAssign(Node assign) {
      Node n = assign.getFirstChild();
      if (n != null && NodeUtil.isVarOrSimpleAssignLhs(n, assign)
          && n.getType() == Token.GETPROP) {
        // We want to exclude the assignment itself from the usage list
        // Check for any chained GETPROP in the object chain where one of the
        // properties is exactly "prototype".
        Node obj = n.getFirstChild();
        while (obj != null && obj.getType() == Token.GETPROP) {
          Node prop = obj.getLastChild();
          int t = prop.getType();
          if ((t == Token.STRING || t == Token.NAME) && "prototype".equals(prop.getString())) {
            return true;
          }
          obj = obj.getFirstChild();
        }
      }

      return false;
    }