public boolean isIndependentOf(Property prop) {
      if (typesRelatedToSet.intersects(prop.typesSet)) {
        return false;
      }
      if (getRelated(prop.type).intersects(typesInSet)) {
        return false;
      }
      return true;
    }