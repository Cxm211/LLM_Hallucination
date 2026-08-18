    private void recordAssignment(NodeTraversal t, Node n, Node recordNode) {
      Node nameNode = n.getFirstChild();
      Node parent = n.getParent();
      NameInformation ns = createNameInformation(t, nameNode);
      if (ns != null) {
        if (parent.isFor() && !NodeUtil.isForIn(parent)) {
          // Patch for assignments that appear in the init,
          // condition or iteration part of a FOR loop.  Without
          // this change, all 3 of those parts try to claim the for
          // loop as their dependency scope.  The last assignment in
          // those three fields wins, which can result in incorrect
          // reference edges between referenced and assigned variables.
          //
          // TODO(user) revisit the dependency scope calculation
          // logic.
          if (parent.getFirstChild().getNext() != n) {
            recordDepScope(recordNode, ns);
          } else {
            recordDepScope(nameNode, ns);
          }
        } else {
          // Determine if this assignment is used as the callee of a call.
          boolean isCallee = false;
          Node callNode = null;
          Node current = n;
          while (current != null) {
            if (NodeUtil.isCall(current)) {
              callNode = current;
              break;
            }
            current = current.getParent();
          }
          if (callNode != null) {
            Node callee = callNode.getFirstChild();
            Node calleeChild = callee;
            // Skip parentheses on the callee.
            while (calleeChild != null && calleeChild != n) {
              if (calleeChild.isParentheses()) {
                calleeChild = calleeChild.getFirstChild();
              } else {
                break;
              }
            }
            if (calleeChild == n) {
              isCallee = true;
            }
          }
          if (isCallee) {
            // The rhs of the assignment is the caller, so it's used by the context.
            // Don't associate it w/ the lhs.
            recordDepScope(recordNode, ns);
          } else {
            // For all other assignments, still use recordNode as before.
            recordDepScope(recordNode, ns);
          }
        }
      }
    }