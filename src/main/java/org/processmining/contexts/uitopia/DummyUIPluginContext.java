package org.processmining.contexts.uitopia;

import org.processmining.contexts.cli.CLIContext;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.connections.Connection;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.ProMFuture;

import javax.inject.Singleton;

@Singleton
public class DummyUIPluginContext extends UIPluginContext {
	public DummyUIPluginContext() {
		super(new CLIContext(), "dummy-context");
	}
	
	@Override
	public <T, C extends Connection> T tryToFindOrConstructFirstObject(Class<T> type, Class<C> connectionType,
                                                                       String role, Object... input) throws ConnectionCannotBeObtained {

		return null;
	}


	public ProMFuture<?> getFutureResult(int var1){
		return new DummyFuture(String.class, null);
	}

//	public void log(String s) {
//		System.out.println(s);
//	}
}
