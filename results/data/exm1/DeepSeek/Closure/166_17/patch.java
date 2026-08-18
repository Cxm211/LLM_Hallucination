public void matchConstraint(JSType constraint) {
    super.matchConstraint(constraint);
    if (hasReferenceName()) {
      return;
    }
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }