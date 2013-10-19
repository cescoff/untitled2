package fr.untitled2.raspi.api;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Batchlet {

    private BatchContext batchContext;

    public abstract void init() throws Exception;

    public abstract boolean isThreadSafe();

    public BatchContext getBatchContext() {
        return batchContext;
    }

    public void setBatchContext(BatchContext batchContext) {
        this.batchContext = batchContext;
    }
}
