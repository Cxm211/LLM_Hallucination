private String formatMethodCall() {
    StringBuilder sb = new StringBuilder();
    sb.append(invocation.getMethod().getName());
    sb.append('(');
    Object[] args = invocation.getArguments();
    if (args != null && args.length > 0) {
        for (int i = 0; i < args.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(args[i] == null ? "null" : args[i].toString());
        }
    }
    sb.append(')');
    return sb.toString();
}