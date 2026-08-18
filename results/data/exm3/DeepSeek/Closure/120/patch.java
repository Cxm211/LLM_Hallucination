    boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          if (block.getParent() == null) {
            break;
          } else {
            return false;
          }
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }