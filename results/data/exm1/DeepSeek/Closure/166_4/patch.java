public void matchConstraint(JSType constraint) {
    if (hasReferenceName()) {
      return;
    }
    super.matchConstraint(constraint);
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }