boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop and not inside a function
      // (since functions may execute multiple times, including via recursion).
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          return false;
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }