public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }

    if (constraint == null) {
      return;
    }

    // Handle the case where the constraint object is a record type.
    // We want to modify the object literal to match the constraint, by
    // taking each property on the record and trying to match
    // properties on this object.
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
      return;
    }

    // Also handle union types that may contain record types.
    UnionType union = constraint.toMaybeUnionType();
    if (union != null) {
      for (JSType alt : union.getAlternates()) {
        if (alt.isRecordType()) {
          matchRecordTypeConstraint(alt.toObjectType());
        }
      }
    }
  }