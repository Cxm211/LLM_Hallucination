String toStringHelper(boolean forAnnotations) {
    if (hasReferenceName()) {
      return getReferenceName();
    }

    // In non-annotation mode, if we're already in a pretty-printing context,
    // just return an elided object representation to avoid recursion.
    if (!forAnnotations && !prettyPrint) {
      return "{...}";
    }

    boolean usePretty = !forAnnotations && prettyPrint;
    if (usePretty) {
      // Don't pretty print recursively.
      prettyPrint = false;
    }

    // Use a tree set so that the properties are sorted.
    Set<String> propertyNames = Sets.newTreeSet();
    for (ObjectType current = this;
         current != null && !current.isNativeObjectType() &&
             (forAnnotations || propertyNames.size() <= MAX_PRETTY_PRINTED_PROPERTIES);
         current = current.getImplicitPrototype()) {
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
      sb.append(forAnnotations
          ? getPropertyType(property).toAnnotationString()
          : getPropertyType(property).toString());

      ++i;
      if (usePretty && i == MAX_PRETTY_PRINTED_PROPERTIES) {
        sb.append(", ...");
        break;
      }
    }

    sb.append("}");

    if (usePretty) {
      prettyPrint = true;
    }
    return sb.toString();
  }