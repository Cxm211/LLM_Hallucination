public boolean apply(JSType type) {
      if (!type.isObjectType()) {
        reportWarning(THIS_TYPE_NON_OBJECT, type.toString());
        return false;
      }
      return true;
    }