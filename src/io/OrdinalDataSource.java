package io;

import java.util.Map;

import data.OrdinalDatum;

public interface OrdinalDataSource {

    public Map<String, Map<String, OrdinalDatum>> getOrdinalData ();

    public Map<String, OrdinalDatum> getOrdinalData (final String columnName);
}
