    private void addNonUnionType(JSType newType) {
      if (skipAmbiguating) {
        return;
      }
      boolean isInterface = false;
      if (newType instanceof ObjectType) {
        isInterface = ((ObjectType) newType).isInterface();
      }
      if (isInvalidatingType(newType) && !isInterface) {
        skipAmbiguating = true;
        return;
      }

      if (type == null) {
        type = newType;
      } else {
        type = type.getLeastSupertype(newType);
      }
      typesSet.set(getIntForType(newType));
    }