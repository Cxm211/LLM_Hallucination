public void matchConstraint(JSType constraint) {
    if (hasReferenceName()) {
      return;
    }
    if (constraint.isObjectType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }