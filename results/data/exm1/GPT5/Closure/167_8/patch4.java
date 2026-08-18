private FlowScope caseNameOrGetProp(Node name, FlowScope blindScope,
      boolean outcome) {
    JSType type = getTypeIfRefinable(name, blindScope);
    if (type != null) {
      JSType restrictedType =
          type.getRestrictedTypeGivenToBooleanOutcome(outcome);
      if (restrictedType == null) {
        return blindScope;
      }
      FlowScope informed = blindScope.createChildFlowScope();
      declareNameInScope(informed, name, restrictedType);
      return informed;
    }
    return blindScope;
  }