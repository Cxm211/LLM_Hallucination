boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      // Start from the parent block to avoid treating assignments in loop initializers
      // as being inside the loop body.
      for (BasicBlock block = ref.getBasicBlock() == null ? null : ref.getBasicBlock().getParent();
           block != null; block = block.getParent()) {
        if (block.isLoop) {
          return false;
        } else if (block.isFunction) {
          break;
        }
      }

      return true;
    }