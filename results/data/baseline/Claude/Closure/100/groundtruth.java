// ===== FIXED com.google.javascript.jscomp.CheckGlobalThis :: shouldReportThis(Node, Node) [lines 146-154] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-100-fixed/src/com/google/javascript/jscomp/CheckGlobalThis.java =====
  private boolean shouldReportThis(Node n, Node parent) {
    if (assignLhsChild != null) {
      // Always report a THIS on the left side of an assign.
      return true;
    }

    // Also report a THIS with a property access.
    return parent != null && NodeUtil.isGet(parent);
  }

// ===== FIXED com.google.javascript.jscomp.CheckGlobalThis :: shouldTraverse(NodeTraversal, Node, Node) [lines 84-135] from /Users/grace/Documents/Paper/BugFixing/Interpretation/defects4j_fixed/Closure/Closure-100-fixed/src/com/google/javascript/jscomp/CheckGlobalThis.java =====
  public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // annotation.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null && (jsDoc.isConstructor() || jsDoc.hasThisType())) {
        return false;
      }

      // Don't traverse functions unless they would normally
      // be able to have a @this annotation associated with them. e.g.,
      // var a = function() { }; // or
      // function a() {} // or
      // a.x = function() {};
      int pType = parent.getType();
      if (!(pType == Token.BLOCK ||
            pType == Token.SCRIPT ||
            pType == Token.NAME ||
            pType == Token.ASSIGN)) {
        return false;
      }
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
