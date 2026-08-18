    boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      if (declaration == null || declaration.getTwin() != null) {
        return false;
      }

      if (isClassOrEnum) {
        return true;
      }

      // If this is a key of an aliased object literal, then it will be aliased
      // later. So we won't be able to collapse its properties.
      if (parent != null && parent.shouldKeepKeys()) {
        return false;
      }

      // If this is aliased, then its properties can't be collapsed either.
      if (type != Type.FUNCTION && aliasingGets > 0) {
        return false;
      }

      return (parent == null || parent.canCollapseUnannotatedChildNames());
    }