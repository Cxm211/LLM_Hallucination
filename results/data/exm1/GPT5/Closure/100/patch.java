public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // annotation.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null && (jsDoc.isConstructor() || jsDoc.hasThisType())) {
        return false;
      }

      // Only traverse functions that could normally have a @this annotation
      // associated with them:
      // - function declarations
      // - function expressions assigned to a name (var a = function(){})
      // - function expressions assigned to a non-prototype property
      if (NodeUtil.isFunctionDeclaration(n)) {
        return true;
      }

      if (parent != null) {
        int pType = parent.getType();
        if (pType == Token.NAME) {
          // var a = function() {}
          return true;
        }
        if (pType == Token.ASSIGN) {
          Node lhs = parent.getFirstChild();
          // Do not traverse prototype methods (allowed to use this).
          if (lhs.getType() == Token.GETPROP) {
            if (lhs.getLastChild().getString().equals("prototype")) {
              return false;
            }
            String leftName = lhs.getQualifiedName();
            if (leftName != null && leftName.contains(".prototype.")) {
              return false;
            }
          }
          // Otherwise, traverse static function assignments.
          return true;
        }
      }

      // Do not traverse anonymous functions in other contexts (e.g., callbacks,
      // return values, standalone expressions).
      return false;
    }

    if (parent != null && parent.getType() == Token.ASSIGN) {
      Node lhs = parent.getFirstChild();
      Node rhs = lhs.getNext();
      
      if (n == lhs) {
        // Always traverse the left side of the assignment. To handle
        // nested assignments properly (e.g., (a = this).property = c;),
        // assignLhsChild should not be overridden.
        if (assignLhsChild == null) {
          assignLhsChild = lhs;
        }
      } else {
        // Only traverse the right side if it's not an assignment to a prototype
        // property or subproperty.
        if (lhs.getType() == Token.GETPROP) {
          if (lhs.getLastChild().getString().equals("prototype")) {
            return false;
          }
          String leftName = lhs.getQualifiedName();
          if (leftName != null && leftName.contains(".prototype.")) {
            return false;
          }
        }
      }
    }

    return true;
  }