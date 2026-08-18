public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }

    if (constraint == null) {
      return;
    }

    // If the constraint is a union, attempt to match each alternate.
    if (constraint.isUnionType()) {
      UnionType union = constraint.toMaybeUnionType();
      if (union != null) {
        for (JSType alt : union.getAlternates()) {
          matchConstraint(alt);
        }
      }
      return;
    }

    // Handle the case where the constraint object is a record type.
    //
    // param constraint {{prop: (number|undefined)}}
    // function f(constraint) {}
    // f({});
    //
    // We want to modify the object literal to match the constraint, by
    // taking any each property on the record and trying to match
    // properties on this object.
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }