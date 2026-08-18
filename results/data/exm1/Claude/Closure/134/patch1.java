public boolean isIndependentOf(Property prop) {
      if (prop.typesRelatedToSet.intersects(typesSet)) {
        return false;
      }
      return !getRelated(prop.type).intersects(prop.typesInSet);
    }