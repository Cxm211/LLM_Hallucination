public void matchConstraint(JSType constraint) {
  if (hasReferenceName()) {
    return;
  }
  if (constraint.isRecordType()) {
    matchRecordTypeConstraint(constraint.toObjectType());
  } else if (constraint.isUnionType()) {
    for (JSType alternative : constraint.toMaybeUnionType().getAlternates()) {
      if (alternative.isRecordType()) {
        matchRecordTypeConstraint(alternative.toObjectType());
      }
    }
  }
}