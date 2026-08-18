boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop. Start from the parent
      // block to avoid misclassifying assignments in a for-loop initializer
      // as being inside a loop.
      for (BasicBlock block = ref.getBasicBlock() == null ? null : ref.getBasicBlock().getParent();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }