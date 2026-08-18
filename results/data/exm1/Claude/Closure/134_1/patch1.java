public boolean isIndependentOf(Property prop) {
      if (typesRelatedToSet.intersects(prop.typesSet)) {
        return false;
      }
      return !prop.typesRelatedToSet.intersects(typesInSet);
    }