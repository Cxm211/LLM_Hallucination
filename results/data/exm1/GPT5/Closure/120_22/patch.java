boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop or inside a function
      // (so only global-scope assignments qualify).
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