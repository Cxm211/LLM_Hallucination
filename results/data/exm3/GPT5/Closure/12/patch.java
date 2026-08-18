private boolean hasExceptionHandler(Node cfgNode) {
    // Walk up the AST to see if this node is inside the try block of a TRY node
    // that has either a catch or finally handler.
    for (Node parent = cfgNode.getParent(); parent != null; parent = parent.getParent()) {
      if (parent.isTry()) {
        Node tryNode = parent;
        Node tryBlock = tryNode.getFirstChild();
        // Check that cfgNode is within the try block (the first child of TRY)
        if (tryBlock != null) {
          for (Node n = cfgNode; n != null; n = n.getParent()) {
            if (n == tryBlock) {
              // Count number of children of TRY. More than 1 implies catch and/or finally present.
              int childCount = 0;
              for (Node c = tryNode.getFirstChild(); c != null; c = c.getNext()) {
                childCount++;
              }
              if (childCount > 1) {
                return true;
              }
              break;
            }
          }
        }
      }
    }
    return false;
  }