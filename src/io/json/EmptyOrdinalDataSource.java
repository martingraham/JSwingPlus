package io.json;

import java.util.Map;

import data.OrdinalDatum;
import io.OrdinalDataSource;

public class EmptyOrdinalDataSource implements OrdinalDataSource {

	@Override
	public Map<String, Map<String, OrdinalDatum>> getOrdinalData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, OrdinalDatum> getOrdinalData (final String columnName) {
		// TODO Auto-generated method stub
		return null;
	}

}
