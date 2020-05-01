package org.processmining.contexts.uitopia;


import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.providedobjects.ProvidedObjectID;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

public class DummyPluginExecutionResult implements PluginExecutionResult {

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void synchronize() throws CancellationException, ExecutionException,
            InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object[] getResults() {
		// TODO Auto-generated method stub
		Object[] ret = new Object[2];
		return ret;
	}

	@Override
	public <T> T getResult(int resultIndex) throws ClassCastException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getResultNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getResultName(int resultIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProvidedObjectID(int i, ProvidedObjectID id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ProvidedObjectID getProvidedObjectID(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Class<? super T> getType(int i) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PluginDescriptor getPlugin() {
		// TODO Auto-generated method stub
		return null;
	}

}
