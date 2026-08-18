boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      BasicBlock start = ref.getBasicBlock();
      if (start == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = start; block != null; block = block.getParent()) {
        if (block.isLoop()) {
          return false;
        } else if (block.isFunction()) {
          break;
        }
      }

      return true;
    }