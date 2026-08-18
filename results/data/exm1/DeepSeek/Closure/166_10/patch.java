  public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (constraint.hasReferenceName()) {
      return;
    }

    // Handle the case where the constraint object is a record type.
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }