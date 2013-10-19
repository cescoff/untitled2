package fr.untitled2.raspi.api;

import fr.untitled2.common.entities.raspi.BatchTaskPayload;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:04 PM
 * To change this template use File | Settings | File Templates.
 */
public interface BatchContextFactory {

    public <T extends Batchlet> BatchContext getBatchContext(BatchTaskPayload batchTaskPayload) throws Exception;

}
