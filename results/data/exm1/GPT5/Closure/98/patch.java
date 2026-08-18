boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Make sure this assignment is not in a loop.
      Node n = ref.getNode();
      if (n != null && NodeUtil.isWithinLoop(n)) {
        return false;
      }

      return true;
    }