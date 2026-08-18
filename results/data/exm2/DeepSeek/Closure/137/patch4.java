    public void addDeclaredName(String name) {
        if (global) {
          reserveName(name);
        } else {
          // It hasn't been declared locally yet, so increment the count.
          if (!declarations.containsKey(name)) {
            int id = incrementNameCount(name);
            String newName = name;
            if (id != 0) {
              newName = getUniqueName(name, id);
            }
            declarations.put(name, newName);
          }
        }
      }