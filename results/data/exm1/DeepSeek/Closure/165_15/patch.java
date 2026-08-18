  public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType() &&
            !greatestSubtype.isNoType() &&
            !greatestSubtype.isNoObjectType() &&
            !greatestSubtype.isAllType()) {
          return true;
        }
      }
    }
    return false;
  }