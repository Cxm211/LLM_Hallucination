boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // If we cannot determine the basic block, be conservative.
      BasicBlock start = ref.getBasicBlock();
      if (start == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = start; block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }