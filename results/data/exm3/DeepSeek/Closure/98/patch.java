    boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      if (NodeUtil.isWithinLoop(ref.getNode())) {
        return false;
      }

      return true;
    }