public void matchConstraint(JSType constraint) {
    if (hasReferenceName()) {
      return;
    }

    if (constraint.isRecordType()) {
      ObjectType objType = constraint.toObjectType();
      if (objType != null) {
        matchRecordTypeConstraint(objType);
      }
    }
  }