boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      BasicBlock block = ref.getBasicBlock();
      if (block != null && block.isFunction) {
        while (block.getParent() != null) {
          if (block.isLoop) {
            return false;
          }
          block = block.getParent();
        }
      }
      return true;
    }