public boolean isIndependentOf(Property prop) {
      if (typesRelatedToSet.intersects(prop.typesSet)) {
        return false;
      }
      return !typesRelatedToSet.intersects(getRelated(prop.type));
    }