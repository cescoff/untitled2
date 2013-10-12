package fr.untitled2.raspi.api;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/7/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Batchlet<F, T> {

    private BatchContext batchContext;

    public abstract void init() throws Exception;

    public abstract T execute(F from) throws Exception;

    public BatchContext getBatchContext() {
        return batchContext;
    }

    public void setBatchContext(BatchContext batchContext) {
        this.batchContext = batchContext;
    }
}
