String toStringHelper(boolean forAnnotations) {
    if (hasReferenceName()) {
      return getReferenceName();
    } else if (prettyPrint) {
      prettyPrint = false;
      Set<String> propertyNames = Sets.newTreeSet();
      Set<ObjectType> visited = Sets.newHashSet();
      for (ObjectType current = this;
           current != null && !current.isNativeObjectType() &&
               propertyNames.size() <= MAX_PRETTY_PRINTED_PROPERTIES &&
               !visited.contains(current);
           current = current.getImplicitPrototype()) {
        visited.add(current);
        propertyNames.addAll(current.getOwnPropertyNames());
      }
      StringBuilder sb = new StringBuilder();
      sb.append("{");
      int i = 0;
      for (String property : propertyNames) {
        if (i > 0) {
          sb.append(", ");
        }
        sb.append(property);
        sb.append(": ");
        sb.append(getPropertyType(property).toString());
        ++i;
        if (i == MAX_PRETTY_PRINTED_PROPERTIES) {
          break;
        }
      }
      if (propertyNames.size() > MAX_PRETTY_PRINTED_PROPERTIES) {
        sb.append(", ...");
      }
      sb.append("}");
      prettyPrint = true;
      return sb.toString();
    } else {
      return "{...}";
    }
  }