boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Allow assignments that appear in loop headers that execute only once
      // (e.g., the initializer of a for-loop, or the RHS of a for-in loop).
      boolean inLoopHeaderExecOnce = false;
      Node n = ref.getNode();
      if (n != null) {
        for (Node p = n.getParent(); p != null; p = p.getParent()) {
          if (p.getType() == Token.FOR) {
            int childCount = p.getChildCount();
            if (childCount == 4) {
              // for (init; cond; incr)
              Node init = p.getFirstChild();
              // Check if n is within the init subtree.
              for (Node a = n; a != null && a != p; a = a.getParent()) {
                if (a == init) { inLoopHeaderExecOnce = true; break; }
              }
            } else if (childCount == 3) {
              // for (lhs in rhs) or for each (lhs in rhs)
              // The second child (rhs) is evaluated once before iteration.
              Node first = p.getFirstChild();
              Node second = first != null ? first.getNext() : null;
              if (second != null) {
                for (Node a = n; a != null && a != p; a = a.getParent()) {
                  if (a == second) { inLoopHeaderExecOnce = true; break; }
                }
              }
            }
            break; // Stop at the nearest enclosing FOR.
          }
          if (p.isFunction()) {
            break;
          }
        }
      }

      // Make sure this assignment is not in a loop (unless it's in a header
      // position that executes only once, as determined above).
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          if (!inLoopHeaderExecOnce) {
            return false;
          }
          // If it's a once-executed loop header, continue checking outer blocks.
        }
      }

      return true;
    }