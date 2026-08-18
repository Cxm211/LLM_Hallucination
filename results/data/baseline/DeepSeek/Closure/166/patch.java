public void matchConstraint(JSType constraint) {
    if (hasReferenceName()) {
      return;
    }

    if (constraint.isRecordType()) {
      matchRecordTypeConstraint(constraint.toObjectType());
    } else if (constraint.isUnionType()) {
      for (JSType alternate : constraint.getAlternates()) {
        matchConstraint(alternate);
      }
    }
  }