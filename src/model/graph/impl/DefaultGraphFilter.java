package model.graph.impl;

import java.util.Collection;

import model.graph.Edge;
import model.graph.GraphFilter;


public class DefaultGraphFilter implements GraphFilter {

	@Override
	public boolean includeEdge (final Edge edge) {
		return true;
	}

	@Override
	public boolean includeNode (final Object obj) {
		return true;
	}
	
	
	static class AndFilter implements GraphFilter {

		Collection<GraphFilter> filterCollection;
		
		AndFilter (final Collection<GraphFilter> filters) {
			filterCollection = filters;
		}
		
		@Override
		public boolean includeEdge (final Edge edge) {
			for (GraphFilter filter : filterCollection) {
				if (!filter.includeEdge (edge))
					return false;
			}
			return true;
		}

		@Override
		public boolean includeNode (final Object obj) {
			for (GraphFilter filter : filterCollection) {
				if (!filter.includeNode (obj))
					return false;
			}
			return true;
		}
		
	}
	

	
	
	static class OrFilter implements GraphFilter {

		Collection<GraphFilter> filterCollection;
		
		OrFilter (final Collection<GraphFilter> filters) {
			filterCollection = filters;
		}
		
		@Override
		public boolean includeEdge (final Edge edge) {
			for (GraphFilter filter : filterCollection) {
				if (filter.includeEdge (edge))
					return true;
			}
			return false;
		}

		@Override
		public boolean includeNode (final Object obj) {
			for (GraphFilter filter : filterCollection) {
				if (filter.includeNode (obj))
					return true;
			}
			return false;
		}	
	}
	
	
	
	static class NotFilter implements GraphFilter {

		GraphFilter filter;
		
		NotFilter (final GraphFilter filters) {
			this.filter = filters;
		}
		
		@Override
		public boolean includeEdge (final Edge edge) {
			return !filter.includeEdge (edge);
		}

		@Override
		public boolean includeNode (final Object obj) {
			return !filter.includeNode (obj);
		}
		
	}
}
