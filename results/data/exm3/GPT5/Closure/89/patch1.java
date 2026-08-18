boolean canCollapseUnannotatedChildNames() {
      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      // Don't try to collapse if the one global set is a twin reference.
      // We could theoretically handle this case in CollapseProperties, but
      // it's probably not worth the effort.
      Preconditions.checkNotNull(declaration);
      if (declaration.getTwin() != null) {
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
      // Exception: allow collapsing for aliased function properties (i.e., when parent != null).
      if (aliasingGets > 0 && (type != Type.FUNCTION || parent == null)) {
        return false;
      }

      return (parent == null || parent.canCollapseUnannotatedChildNames());
    }