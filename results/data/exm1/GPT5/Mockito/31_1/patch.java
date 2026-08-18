private String formatMethodCall() {
    StringBuilder sb = new StringBuilder();
    sb.append(invocation.getMethod().getName()).append("(");
    Object[] args = invocation.getArguments();
    if (args != null) {
        for (int i = 0; i < args.length; i++) {
            sb.append(formatArg(args[i]));
            if (i < args.length - 1) sb.append(", ");
        }
    }
    sb.append(")");
    return sb.toString();
}

private String formatArg(Object arg) {
    if (arg == null) return "null";
    Class<?> c = arg.getClass();
    if (c.isArray()) {
        if (arg instanceof Object[]) return java.util.Arrays.deepToString((Object[]) arg);
        if (arg instanceof int[]) return java.util.Arrays.toString((int[]) arg);
        if (arg instanceof long[]) return java.util.Arrays.toString((long[]) arg);
        if (arg instanceof short[]) return java.util.Arrays.toString((short[]) arg);
        if (arg instanceof byte[]) return java.util.Arrays.toString((byte[]) arg);
        if (arg instanceof char[]) return java.util.Arrays.toString((char[]) arg);
        if (arg instanceof boolean[]) return java.util.Arrays.toString((boolean[]) arg);
        if (arg instanceof float[]) return java.util.Arrays.toString((float[]) arg);
        if (arg instanceof double[]) return java.util.Arrays.toString((double[]) arg);
    }
    return String.valueOf(arg);
}