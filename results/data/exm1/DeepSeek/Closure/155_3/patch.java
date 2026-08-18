    private void doInlinesForScope(NodeTraversal t,
        Map<Var, ReferenceCollection> referenceMap) {

      for (Iterator<Var> it = t.getScope().getVars(); it.hasNext();) {
        Var v = it.next();

        ReferenceCollection referenceInfo = referenceMap.get(v);

        if (referenceInfo == null || isVarInlineForbidden(v)) {
          continue;
        } else if (isInlineableDeclaredConstant(v, referenceInfo)) {
          Reference init = referenceInfo.getInitializingReferenceForConstants();
          if (init != null) {
            Node value = init.getAssignedValue();
            inlineDeclaredConstant(v, value, referenceInfo.references);
            staleVars.add(v);
          }
        } else if (mode == Mode.CONSTANTS_ONLY) {
          continue;
        } else {
          inlineNonConstants(v, referenceInfo);
        }
      }
    }
