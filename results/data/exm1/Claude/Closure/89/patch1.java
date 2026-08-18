boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      Preconditions.checkNotNull(declaration);
      if (declaration.getTwin() != null) {
        return false;
      }

      if (isClassOrEnum) {
        return true;
      }

      if (parent != null && parent.shouldKeepKeys()) {
        return false;
      }

      if (type != Type.FUNCTION && aliasingGets > 0) {
        return false;
      }

      return (parent == null || parent.canCollapseUnannotatedChildNames());
    }