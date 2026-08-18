boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop or inside a function body
      // (since functions may execute multiple times).
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isLoop) {
          return false;
        } else if (block.isFunction) {
          return false;
        }
      }

      return true;
    }