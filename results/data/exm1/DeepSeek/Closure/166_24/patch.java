public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    // (removed early return for named types as it breaks symmetry)
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    }
  }