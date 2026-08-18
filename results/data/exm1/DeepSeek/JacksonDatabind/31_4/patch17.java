    protected final void _append(JsonToken type, Object value)
    {
        Segment next = _hasNativeId
                ? _last.append(_appendAt, type, value, _objectId, _typeId)
                : _last.append(_appendAt, type, value);
        if (next == null) {
            ++_appendAt;
        } else {
            _last = next;
            _appendAt = 1;
        }
    }