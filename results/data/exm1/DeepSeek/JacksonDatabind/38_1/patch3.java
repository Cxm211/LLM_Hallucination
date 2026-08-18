public StringBuilder getGenericSignature(StringBuilder sb)
{
    _classSignature(_class, sb, false);

    final int count = _bindings.size();
    if (count > 0) {
        sb.append('<');
        for (int i = 0; i < count; ++i) {
            sb = containedType(i).getGenericSignature(sb);
        }
        sb.append('>');
    }
    sb.append(';');
    return sb;
}