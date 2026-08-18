public void matchConstraint(JSType constraint) {
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
    if (hasReferenceName()) {
      return;
    }
}