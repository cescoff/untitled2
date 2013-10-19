package fr.untitled2.raspi.api;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/7/13
 * Time: 11:18 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SlaveBatchlet<F, T> extends Batchlet {

    public abstract T execute(F from) throws Exception;

    public abstract Class<F> getInputType();

}
