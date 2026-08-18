    private void addNonUnionType(JSType newType) {
      if (skipAmbiguating || isInvalidatingType(newType)) {
        skipAmbiguating = true;
        return;
      }

      if (type == null) {
        type = newType;
      } else {
        type = type.getLeastSupertype(newType);
      }
      typesSet.set(getIntForType(newType));
      if (relatedSet == null) {
        relatedSet = new BitSet();
      }
      relatedSet.or(getRelated(newType));
    }