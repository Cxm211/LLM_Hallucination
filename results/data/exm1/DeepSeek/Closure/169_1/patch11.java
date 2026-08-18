  static boolean isSubtype(ObjectType typeA, RecordType typeB) {
    for (String property : typeB.properties.keySet()) {
      if (!typeA.hasProperty(property)) {
        return false;
      }

      JSType propA = typeA.getPropertyType(property);
      JSType propB = typeB.getPropertyType(property);
      if (!propA.isUnknownType() && !propB.isUnknownType()) {
      if (typeA.isPropertyTypeDeclared(property)) {
        if (!propA.isInvariant(propB)) {
          return false;
        }
      } else {
        if (!propA.isSubtype(propB)) {
          return false;
          }
        }
      }
    }

    return true;
  }