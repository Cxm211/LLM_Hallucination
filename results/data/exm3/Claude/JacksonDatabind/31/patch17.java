protected final void _append(JsonToken type)
{
    Segment next = _hasNativeId
            ? _last.append(_appendAt, type, _objectId, _typeId)
            : _last.append(_appendAt, type);
    if (next == null) {
        ++_appendAt;
    } else {
        _last = next;
        _appendAt = 1;
    }
}