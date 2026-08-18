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
          // Determine if the assignment is in the function part of a call.
          boolean isInCaller = false;
          Node ancestor = n;
          while (ancestor != null) {
            if (NodeUtil.isCall(ancestor)) {
              // Check if n is in the subtree of ancestor.getFirstChild()
              Node func = ancestor.getFirstChild();
              Node current = n;
              while (current != null && current != func) {
                current = current.getParent();
              }
              if (current == func) {
                isInCaller = true;
                break;
              }
            }
            ancestor = ancestor.getParent();
          }
          if (isInCaller) {
            // The rhs of the assignment is the caller, so it's used by the
            // context. Don't associate it w/ the lhs.
            recordDepScope(recordNode, ns);
          } else {
            // Otherwise, associate with the lhs.
            recordDepScope(nameNode, ns);
          }
        }
      }
    }