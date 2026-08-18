  public void matchConstraint(JSType constraint) {
    // We only want to match constraints on anonymous types.
    if (hasReferenceName()) {
      return;
    }

    // Handle the case where the constraint object is a record type.
    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    } else if (constraint.isUnionType()) {
      for (JSType alternate : constraint.toUnionType().getAlternates()) {
        matchConstraint(alternate);
      }
    }
  }