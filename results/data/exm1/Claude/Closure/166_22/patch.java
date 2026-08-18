public void matchConstraint(JSType constraint) {
    if (constraint == null) {
      return;
    }
    if (hasReferenceName()) {
      return;
    }

    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }