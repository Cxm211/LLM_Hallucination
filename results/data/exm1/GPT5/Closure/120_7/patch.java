boolean isAssignedOnceInLifetime() {
      Reference ref = getOneAndOnlyAssignment();
      if (ref == null) {
        return false;
      }

      // Previously disallowed assignments within loops. This was overly
      // conservative and prevented valid inlining in some cases.
      return true;
    }