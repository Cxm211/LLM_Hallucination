public boolean shouldTraverse(NodeTraversal t, Node n, Node parent) {

    if (n.getType() == Token.FUNCTION) {
      // Don't traverse functions that are constructors or have the @this
      // or @override annotation, or are interfaces.
      JSDocInfo jsDoc = getFunctionJsDocInfo(n);
      if (jsDoc != null &&
          (jsDoc.isConstructor() ||
           jsDoc.hasThisType() ||
           jsDoc.isOverride() ||
           jsDoc.isInterface())) {
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
        // property or its direct subproperty (i.e., a.prototype or a.prototype.x).
        boolean skipRhs = false;

        // Helper: detect direct prototype target or direct property of prototype
        // via qualified name (covers simple GETPROP chains).
        String qName = lhs.getQualifiedName();
        if (qName != null) {
          if (qName.endsWith(".prototype") || qName.equals("prototype")) {
            skipRhs = true;
          } else {
            int idx = qName.indexOf(".prototype.");
            if (idx >= 0) {
              String after = qName.substring(idx + ".prototype.".length());
              // Direct property of prototype has no further dots after the first property name.
              if (after.indexOf('.') < 0) {
                skipRhs = true;
              }
            }
          }
        }

        // Structural checks to handle computed properties (GETELEM) where
        // qualified names are not available.
        if (!skipRhs) {
          int lhsType = lhs.getType();
          if (lhsType == Token.GETPROP) {
            // a['prototype'].x => base is GETELEM whose base is a.prototype
            Node base = lhs.getFirstChild();
            if (base != null && base.getType() == Token.GETELEM) {
              Node baseObj = base.getFirstChild();
              if (baseObj != null && baseObj.getType() == Token.GETPROP &&
                  baseObj.getLastChild() != null && "prototype".equals(baseObj.getLastChild().getString())) {
                skipRhs = true;
              }
            }
            // a.prototype (assigning directly to prototype object)
            if (!skipRhs && lhs.getLastChild() != null && "prototype".equals(lhs.getLastChild().getString())) {
              skipRhs = true;
            }
          } else if (lhsType == Token.GETELEM) {
            // a.prototype['x'] or a['prototype'][expr]
            Node base = lhs.getFirstChild();
            if (base != null) {
              if (base.getType() == Token.GETPROP) {
                if (base.getLastChild() != null && "prototype".equals(base.getLastChild().getString())) {
                  skipRhs = true;
                }
              } else if (base.getType() == Token.GETELEM) {
                Node baseObj = base.getFirstChild();
                if (baseObj != null && baseObj.getType() == Token.GETPROP &&
                    baseObj.getLastChild() != null && "prototype".equals(baseObj.getLastChild().getString())) {
                  skipRhs = true;
                }
              }
            }
          }
        }

        if (skipRhs) {
          return false;
        }
      }
    }

    return true;
  }