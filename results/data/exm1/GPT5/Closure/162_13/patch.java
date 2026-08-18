public Iterator<Var> getVars() {
  java.util.List<Var> list = new java.util.ArrayList<Var>(vars.values());
  java.util.Collections.sort(list, new java.util.Comparator<Var>() {
    public int compare(Var a, Var b) {
      return a.getName().compareTo(b.getName());
    }
  });
  return list.iterator();
}