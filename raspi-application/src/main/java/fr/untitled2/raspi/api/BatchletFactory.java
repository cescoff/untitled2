package fr.untitled2.raspi.api;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BatchletFactory {

    public <T extends Batchlet> Batchlet getBatchlet(Class<T> batchletClass) throws Exception;

}
