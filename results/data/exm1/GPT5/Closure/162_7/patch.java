public Iterator<Var> getVars() {
    return java.util.Collections.unmodifiableCollection(vars.values()).iterator();
  }