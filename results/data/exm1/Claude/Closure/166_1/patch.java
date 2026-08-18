public void matchConstraint(JSType constraint) {
    if (hasReferenceName()) {
      return;
    }

    if (constraint == null) {
      return;
    }

    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }