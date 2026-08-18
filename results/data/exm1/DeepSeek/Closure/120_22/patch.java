boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      for (BasicBlock block = ref.getBasicBlock();
           block != null; block = block.getParent()) {
        if (block.isFunction) {
          // Only break if the variable is defined in this function,
          // because the variable's lifetime is limited to the function call.
          if (block.getFunction() == this.getScope().getRootNode()) {
            break;
          }
        } else if (block.isLoop) {
          return false;
        }
      }

      return true;
    }