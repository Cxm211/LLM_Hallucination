  public void matchConstraint(JSType constraint) {
    // Handle the case where the constraint object is a record type.
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
      return;
    }

    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }
  }