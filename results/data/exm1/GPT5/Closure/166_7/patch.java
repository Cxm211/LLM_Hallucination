  public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }

    if (constraint == null) {
      return;
    }

    // Handle the case where the constraint object is a record type or any object type.
    // We want to modify the object literal to match the constraint, by
    // taking any each property on the record and trying to match
    // properties on this object.
    if (constraint.isObject()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }