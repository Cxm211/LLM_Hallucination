  public boolean canPropertyBeDefined(JSType type, String propertyName) {
    if (typesIndexedByProperty.containsKey(propertyName)) {
      for (JSType alt :
               typesIndexedByProperty.get(propertyName).getAlternates()) {
        if (alt.isNoType() || alt.isNoObjectType() || alt.isAllType()) {
          continue;
        }
        JSType greatestSubtype = alt.getGreatestSubtype(type);
        if (!greatestSubtype.isEmptyType()) {
          return true;
        }
      }
    }
    return false;
  }