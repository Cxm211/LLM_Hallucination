  String toStringHelper(boolean forAnnotations) {
    if (hasReferenceName()) {
      return getReferenceName();
    } else if (prettyPrint) {
      // Don't pretty print recursively.
      prettyPrint = false;

      // Use a tree set so that the properties are sorted.
      Set<String> propertyNames = Sets.newTreeSet();
      int maxProperties = forAnnotations ? Integer.MAX_VALUE : MAX_PRETTY_PRINTED_PROPERTIES;
      for (ObjectType current = this;
           current != null && !current.isNativeObjectType() &&
               propertyNames.size() <= maxProperties;
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
        sb.append(getPropertyType(property).toString());

        ++i;
        if (!forAnnotations && i == MAX_PRETTY_PRINTED_PROPERTIES) {
          sb.append(", ...");
          break;
        }
      }

      sb.append("}");

      prettyPrint = true;
      return sb.toString();
    } else {
      // Recursive call; for annotations, use "?" else "{...}"
      return forAnnotations ? "?" : "{...}";
    }
  }