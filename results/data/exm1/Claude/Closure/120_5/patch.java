boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          return false;
        }
      }

      // Make sure this assignment is not in a conditional block
      // unless it's at the function level
      BasicBlock block = ref.getBasicBlock();
      if (block != null && block.getParent() != null && !block.getParent().isFunction) {
        return false;
      }

      return true;
    }