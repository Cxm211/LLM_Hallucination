public Iterator<Var> getVars() {
    return vars != null ? vars.values().iterator() : java.util.Collections.<Var>emptyList().iterator();
  }