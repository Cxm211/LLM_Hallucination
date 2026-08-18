public void matchConstraint(JSType constraint) {
    if (hasReferenceName()) {
      return;
    }
    if (constraint.isRecordType()) {
      ObjectType obj = constraint.toObjectType();
      if (obj != null) {
        matchRecordTypeConstraint(obj);
      }
    }
  }