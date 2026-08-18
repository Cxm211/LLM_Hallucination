private void calculateHashCode(Object[] keys)
	{
		int total = 1;
        for (int i = 0; i < keys.length; i++) {
            int elementHash = (keys[i] == null ? 0 : keys[i].hashCode());
            total = 31 * total + elementHash;
        }
        hashCode = total;
	}