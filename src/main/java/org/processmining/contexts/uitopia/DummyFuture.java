package org.processmining.contexts.uitopia;


import org.processmining.framework.plugin.ProMFuture;

public class DummyFuture extends ProMFuture {

	
	public DummyFuture(Class resultClass, String label) {
		super(resultClass, label);
		// TODO Auto-generated constructor stub
	}

	public void setLabel(String x) {
		
	}

	@Override
	protected Object doInBackground() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
}
