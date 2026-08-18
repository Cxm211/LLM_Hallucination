boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      Node assignNode = ref.getNode();
      if (NodeUtil.isInLoop(assignNode)) {
        return false;
      }

      return true;
    }