boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop. Start with the parent block
      // to avoid treating a for-loop initializer as inside the loop.
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