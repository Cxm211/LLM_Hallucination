    public boolean isIndependentOf(Property prop) {
      if (typesInSet.intersects(prop.typesSet)) {
        return false;
      }
      if (typesRelatedToSet.intersects(prop.typesSet)) {
        return false;
      }
      return !getRelated(prop.type).intersects(typesInSet);
    }