private void calculateHashCode(Object[] keys)
	{
		int total = 1;
        for (int i = 0; i < keys.length; i++) {
            total = 31 * total + (keys[i] == null ? 0 : keys[i].hashCode());
        }
        hashCode = total;
	}
