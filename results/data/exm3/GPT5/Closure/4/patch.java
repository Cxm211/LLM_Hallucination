JSType resolveInternal(ErrorReporter t, StaticScope<JSType> enclosing) {
    // TODO(user): Investigate whether it is really necessary to keep two
    // different mechanisms for resolving named types, and if so, which order
    // makes more sense. Now, resolution via registry is first in order to
    // avoid triggering the warnings built into the resolution via properties.
    boolean resolved = resolveViaRegistry(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
    }

    if (resolved) {
      super.resolveInternal(t, enclosing);
      // After resolution via registry, cycles may become apparent only after
      // the super call. Re-check and handle any cycles found now.
      if (detectImplicitPrototypeCycle()) {
        handleTypeCycle(t);
      }
      if (isResolved()) {
        finishPropertyContinuations();
      }
      return registry.isLastGeneration() ?
          getReferencedType() : this;
    }

    resolveViaProperties(t, enclosing);
    if (detectImplicitPrototypeCycle()) {
      handleTypeCycle(t);
    }

    super.resolveInternal(t, enclosing);
    if (isResolved()) {
      finishPropertyContinuations();
    }
    return registry.isLastGeneration() ?
        getReferencedType() : this;
  }