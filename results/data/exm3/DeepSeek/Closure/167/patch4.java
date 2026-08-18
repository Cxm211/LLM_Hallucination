  private FlowScope caseNameOrGetProp(Node name, FlowScope blindScope,
      boolean outcome) {
    JSType type = getTypeIfRefinable(name, blindScope);
    if (type != null) {
      JSType restrictedType =
          type.getRestrictedTypeGivenToBooleanOutcome(outcome);
      if (!restrictedType.equals(getNativeType(JSTypeNative.NO_TYPE))) {
        FlowScope informed = blindScope.createChildFlowScope();
        declareNameInScope(informed, name, restrictedType);
        return informed;
      }
    }
    return blindScope;
  }