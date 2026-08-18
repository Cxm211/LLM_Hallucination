  public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }

    // Handle the case where the constraint object is a record type.
    if (constraint.isRecordType()) {
      // Avoid infinite recursion when both types are record types.
      if (this.isRecordType()) {
        return;
      }
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }