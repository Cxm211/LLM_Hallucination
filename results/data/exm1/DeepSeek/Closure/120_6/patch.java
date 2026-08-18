    boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      BasicBlock block = ref.getBasicBlock();
      if (block == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (; block != null; block = block.getParent()) {
        if (block.isFunction) {
          break;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }