package fr.untitled2.raspi.api;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:36 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class MasterBatchlet extends Batchlet {

    public abstract void execute() throws Exception;

}
