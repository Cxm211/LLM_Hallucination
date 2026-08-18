  public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (!typesIndexedByProperty.containsKey(propertyName)) {
      return true;
    }
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType()) {
          return true;
        }
      }
    }
    return false;
  }
