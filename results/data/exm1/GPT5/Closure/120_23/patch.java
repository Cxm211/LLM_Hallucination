boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop, including loops in outer scopes.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isLoop) {
          return false;
        }
        // Do not break at function boundaries; continue checking outer blocks.
      }

      return true;
    }