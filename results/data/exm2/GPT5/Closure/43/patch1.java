    public void visit(NodeTraversal t, Node n, Node parent) {
      inputId = t.getInputId();
      attachLiteralTypes(t, n);

      switch (n.getType()) {
        case Token.CALL:
          checkForClassDefiningCalls(t, n, parent);
          checkForCallingConventionDefiningCalls(n, delegateCallingConventions);
          break;

        case Token.FUNCTION:
          if (t.getInput() == null || !t.getInput().isExtern()) {
            nonExternFunctions.add(n);
          }

          // Hoisted functions are handled during pre-traversal.
          if (!NodeUtil.isHoistedFunctionDeclaration(n)) {
            defineFunctionLiteral(n, parent);
          }
          break;

        case Token.ASSIGN:
          // Handle initialization of properties.
          Node firstChild = n.getFirstChild();
          if (firstChild.isGetProp() &&
              firstChild.isQualifiedName()) {
            maybeDeclareQualifiedName(t, n.getJSDocInfo(),
                firstChild, n, firstChild.getNext());
          }
          break;

        case Token.CATCH:
          defineCatch(n, parent);
          break;

        case Token.VAR:
          defineVar(n, parent);
          break;

        case Token.GETPROP:
          // Handle stubbed properties.
          if (parent.isExprResult() &&
              n.isQualifiedName()) {
            maybeDeclareQualifiedName(t, n.getJSDocInfo(), n, parent, null);
          }
          break;
      }

      // Analyze any @lends object literals in this statement.
      java.util.ArrayDeque<Node> __stack = new java.util.ArrayDeque<Node>();
      for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
        __stack.push(c);
      }
      while (!__stack.isEmpty()) {
        Node cur = __stack.pop();
        if (cur.isObjectLit()) {
          JSDocInfo info = cur.getJSDocInfo();
          if (info != null && info.getLendsName() != null) {
            defineObjectLiteral(cur);
          }
        }
        for (Node child = cur.getFirstChild(); child != null; child = child.getNext()) {
          __stack.push(child);
        }
      }
    }