package org.processmining.contexts.uitopia;

import org.deckfour.xes.model.XLog;
import org.processmining.contexts.cli.CLIContext;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.plugin.ProMFuture;
import org.processmining.framework.plugin.impl.ProgressBarImpl;

import javax.inject.Singleton;
import java.util.concurrent.Executor;

@Singleton
public class DummyPluginContext
		extends org.processmining.framework.plugin.impl.AbstractPluginContext {

	/**
	 * 
	 * @param label
	 */
	public DummyPluginContext(String label) {

		super(new CLIContext(), label);
		progress = new ProgressBarImpl(this);
	}

	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected PluginContext createTypedChildContext(String label) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PluginExecutionResult getResult() {
		PluginExecutionResult ret = new DummyPluginExecutionResult();
		return ret;
	}

	@Override
	public ProMFuture<?> getFutureResult(int i) {
		DummyFuture x = new DummyFuture(XLog.class, "x");
		return x;
	}
}
