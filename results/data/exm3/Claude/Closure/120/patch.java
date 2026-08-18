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

      // Check if the variable is assigned in a function that can be called multiple times
      // If there's a recursive call or the function containing the assignment can be invoked
      // multiple times, then it's not assigned once in lifetime
      BasicBlock assignmentBlock = ref.getBasicBlock();
      if (assignmentBlock != null) {
        // Find the function containing this assignment
        BasicBlock functionBlock = assignmentBlock;
        while (functionBlock != null && !functionBlock.isFunction) {
          functionBlock = functionBlock.getParent();
        }
        
        // If the assignment is inside a function (not global scope),
        // we need to ensure the function is not called multiple times
        if (functionBlock != null && functionBlock.getParent() != null) {
          // The assignment is in a nested function, which can be called multiple times
          // This means the variable can be assigned multiple times in its lifetime
          return false;
        }
      }

      return true;
    }