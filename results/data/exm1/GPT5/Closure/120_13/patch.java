boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        // Check for loops before stopping at a function boundary.
        if (block.isLoop) {
          return false;
        } else if (block.isFunction) {
          break;
        }
      }

      return true;
    }