public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null &&
          (jsDoc.isConstructor() ||
           jsDoc.isInterface() ||
           jsDoc.hasThisType() ||
           jsDoc.isOverride())) {
        return false;
      }

      int pType = parent.getType();
      if (!(pType == Token.BLOCK ||
            pType == Token.SCRIPT ||
            pType == Token.NAME ||
            pType == Token.ASSIGN ||
            pType == Token.STRING_KEY)) {
        return false;
      }

      if (pType == Token.STRING_KEY) {
        Node objLit = parent.getParent();
        if (objLit != null && objLit.getType() == Token.OBJECTLIT) {
          Node assign = objLit.getParent();
          if (assign != null && assign.getType() == Token.ASSIGN) {
            Node lhs = assign.getFirstChild();
            if (lhs != null && NodeUtil.isGet(lhs)) {
              if (lhs.getType() == Token.GETPROP &&
                  lhs.getLastChild().getString().equals("prototype")) {
                return false;
              }
              Node llhs = lhs.getFirstChild();
              if (llhs != null && llhs.getType() == Token.GETPROP &&
                  llhs.getLastChild().getString().equals("prototype")) {
                return false;
              }
            }
          }
        }
      }
    }

    if (parent != null && parent.getType() == Token.ASSIGN) {
      Node lhs = parent.getFirstChild();
      Node rhs = lhs.getNext();

      if (n == lhs) {
        if (assignLhsChild == null) {
          assignLhsChild = lhs;
        }
      } else {
        if (NodeUtil.isGet(lhs)) {
          if (lhs.getType() == Token.GETPROP &&
              lhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
          Node llhs = lhs.getFirstChild();
          if (llhs.getType() == Token.GETPROP &&
              llhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
        }
      }
    }

    return true;
  }