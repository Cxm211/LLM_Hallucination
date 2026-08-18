private void doInlinesForScope(NodeTraversal t,
    Map<Var, ReferenceCollection> referenceMap) {

  List<Var> vars = new ArrayList<Var>();
  for (Iterator<Var> it = t.getScope().getVars(); it.hasNext();) {
    vars.add(it.next());
  }

  for (Var v : vars) {
    ReferenceCollection referenceInfo = referenceMap.get(v);

    if (referenceInfo == null || isVarInlineForbidden(v)) {
      continue;
    } else if (isInlineableDeclaredConstant(v, referenceInfo)) {
      Reference init = referenceInfo.getInitializingReferenceForConstants();
      Node value = init.getAssignedValue();
      inlineDeclaredConstant(v, value, referenceInfo.references);
      staleVars.add(v);
    } else if (mode == Mode.CONSTANTS_ONLY) {
      continue;
    } else {
      inlineNonConstants(v, referenceInfo);
    }
  }
}