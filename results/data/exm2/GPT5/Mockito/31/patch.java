private String formatMethodCall() {
    StringBuilder sb = new StringBuilder();
    sb.append(invocation.getMethod().getName()).append("(");
    Object[] args = null;
    try {
        args = invocation.getArguments();
    } catch (Throwable t) {
        // fallback
    }
    if (args == null) {
        args = new Object[0];
    }
    for (int i = 0; i < args.length; i++) {
        sb.append(String.valueOf(args[i]));
        if (i < args.length - 1) {
            sb.append(", ");
        }
    }
    sb.append(")");
    return sb.toString();
}