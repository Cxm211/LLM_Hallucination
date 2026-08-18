protected void _reportTooLongIntegral(int expType, String rawNum) throws IOException
    {
        final String numDesc = _longIntegerDesc(rawNum);
        String msg = String.format("Numeric value (%s) out of range of %s", numDesc,
                (expType == NR_LONG) ? "long" : "int");
        _reportError(msg);
    }