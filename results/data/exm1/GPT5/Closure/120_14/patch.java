boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop and not inside a function
      // (i.e., it only happens once for the whole program lifetime).
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          // Inside a function body: could execute multiple times.
          return false;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }