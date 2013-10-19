package fr.untitled2.raspi.api.impl;

import com.google.common.collect.Maps;
import fr.untitled2.raspi.api.Batchlet;
import fr.untitled2.raspi.api.BatchletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 10:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleBatchletFactory implements BatchletFactory {

    @Override
    public <T extends Batchlet> Batchlet getBatchlet(Class<T> batchletClass) throws Exception {
        try {
            Batchlet batchlet =  batchletClass.newInstance();
            batchlet.init();
            return batchlet;
        } catch (Throwable t) {
            throw new Exception("Class '" + batchletClass + "' cannot be loaded", t);
        }
    }
}
