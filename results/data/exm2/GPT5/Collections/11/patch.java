private void calculateHashCode(Object[] keys)
	{
		int total = 0;
        for (int i = 0; i < keys.length; i++) {
            int h = (keys[i] == null) ? 0 : keys[i].hashCode();
            total = 31 * total + h;
        }
        hashCode = total;
	}