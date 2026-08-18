    private void inlineNonConstants(
        Var v, ReferenceCollection referenceInfo) {
      int refCount = referenceInfo.references.size();
      Reference declaration = referenceInfo.references.get(0);
      Reference init = referenceInfo.getInitializingReference();
      int firstRefAfterInit = (declaration == init) ? 2 : 3;

      if (refCount > 1 &&
          isImmutableAndWellDefinedVariable(v, referenceInfo)) {
        // if the variable is referenced more than once, we can only
        // inline it if it's immutable and never defined before referenced.
        Node value;
        if (init != null) {
          value = init.getAssignedValue();
        } else {
          // Create a new node for variable that is never initialized.
          Node srcLocation = declaration.getNameNode();
          value = NodeUtil.newUndefinedNode(srcLocation);
        }
        Preconditions.checkNotNull(value);
        inlineWellDefinedVariable(v, value, referenceInfo.references);
        staleVars.add(v);
      } else if (refCount == firstRefAfterInit) {
        // The variable likely only read once, try some more
        // complex inlining heuristics.
        Reference reference = referenceInfo.references.get(
            firstRefAfterInit - 1);
        if (canInline(declaration, init, reference)) {
          inline(v, declaration, init, reference);
          staleVars.add(v);
        }
      } else if (declaration != init && refCount == 2) {
        if (isValidDeclaration(declaration) && isValidInitialization(init)) {
          // The only reference is the initialization, remove the assignment and
          // the variable declaration.
          Node value = init.getAssignedValue();
          Preconditions.checkNotNull(value);
          inlineWellDefinedVariable(v, value, referenceInfo.references);
          staleVars.add(v);
        }
      }

      // If this variable was not inlined normally, check if we can
      // inline an alias of it. (If the variable was inlined, then the
      // reference data is out of sync. We're better off just waiting for
      // the next pass.)
      if (
          !staleVars.contains(v) && referenceInfo.isWellDefined() &&
          referenceInfo.isAssignedOnceInLifetime()) {
        // Check if the variable name is "arguments" - if so, do not inline aliases
        // if arguments is potentially modified or escaped in the same scope
        boolean isArguments = v.getName().equals("arguments");
        if (isArguments) {
          // Check if arguments is modified or escaped in this scope
          boolean argumentsModifiedOrEscaped = false;
          for (Reference ref : referenceInfo.references) {
            Node refNode = ref.getNameNode();
            Node refParent = refNode.getParent();
            // Check if arguments is modified (assigned to)
            if (refParent != null && NodeUtil.isLValue(refNode)) {
              argumentsModifiedOrEscaped = true;
              break;
            }
            // Check if arguments[i] is assigned to
            if (refParent != null && refParent.isGetElem() && refParent.getFirstChild() == refNode) {
              Node getElemParent = refParent.getParent();
              if (getElemParent != null && NodeUtil.isAssignmentOp(getElemParent) && getElemParent.getFirstChild() == refParent) {
                argumentsModifiedOrEscaped = true;
                break;
              }
            }
            // Check if arguments is passed to a function (escaped)
            if (refParent != null && refParent.isCall() && refNode != refParent.getFirstChild()) {
              argumentsModifiedOrEscaped = true;
              break;
            }
          }
          if (argumentsModifiedOrEscaped) {
            return; // Skip alias inlining for this variable
          }
        }

        List<Reference> refs = referenceInfo.references;
        for (int i = 1 /* start from a read */; i < refs.size(); i++) {
          Node nameNode = refs.get(i).getNameNode();
          if (aliasCandidates.containsKey(nameNode)) {
            AliasCandidate candidate = aliasCandidates.get(nameNode);
            if (!staleVars.contains(candidate.alias) &&
                !isVarInlineForbidden(candidate.alias)) {
              Reference aliasInit;
              aliasInit = candidate.refInfo.getInitializingReference();
              Node value = aliasInit.getAssignedValue();
              Preconditions.checkNotNull(value);
              inlineWellDefinedVariable(candidate.alias,
                  value,
                  candidate.refInfo.references);
              staleVars.add(candidate.alias);
            }
          }
        }
      }
    }