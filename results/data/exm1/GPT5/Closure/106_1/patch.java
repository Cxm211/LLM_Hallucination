boolean canCollapseUnannotatedChildNames() {
      // If this name represents a class or enum, allow collapsing its unannotated
      // child names, but still ensure that the parent (if any) can be collapsed.
      if (isClassOrEnum) {
        return parent == null || parent.canCollapseUnannotatedChildNames();
      }

      if (type == Type.OTHER || globalSets != 1 || localSets != 0) {
        return false;
      }

      // Don't try to collapse if the one global set is a twin reference.
      // We could theoretically handle this case in CollapseProperties, but
      // it's probably not worth the effort.

      return (type == Type.FUNCTION || aliasingGets == 0) &&
          (parent == null || parent.canCollapseUnannotatedChildNames());
    }