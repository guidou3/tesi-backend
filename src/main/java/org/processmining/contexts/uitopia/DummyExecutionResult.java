package org.processmining.contexts.uitopia;

import org.processmining.framework.plugin.PluginDescriptor;
import org.processmining.framework.plugin.PluginExecutionResult;
import org.processmining.framework.providedobjects.ProvidedObjectID;

import javax.inject.Singleton;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

@Singleton
public class DummyExecutionResult implements PluginExecutionResult {
    public int getSize(){
        return 0;
    }

    public void synchronize() throws CancellationException, ExecutionException,
            InterruptedException {

    }

    public Object[] getResults(){
        return null;
    }

    public <T> T getResult(int var1) throws ClassCastException {
        return null;

    }

    public String[] getResultNames(){
        return null;

    }

    public String getResultName(int var1){
        return null;

    }

    public void setProvidedObjectID(int var1, ProvidedObjectID var2){

    }

    public ProvidedObjectID getProvidedObjectID(int var1){
        return null;

    }

    public <T> Class<? super T> getType(int var1){
        return null;

    }

    public PluginDescriptor getPlugin(){
        return null;

    }
}
