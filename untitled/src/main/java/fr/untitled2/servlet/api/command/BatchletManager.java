package fr.untitled2.servlet.api.command;

import com.googlecode.objectify.ObjectifyService;
import fr.untitled2.common.entities.raspi.AvailableBatchlets;
import fr.untitled2.common.entities.raspi.BatchletPayLoad;
import fr.untitled2.common.entities.raspi.SimpleResponse;
import fr.untitled2.entities.BatchServer;
import fr.untitled2.entities.Batchlet;
import fr.untitled2.entities.User;
import fr.untitled2.utils.SignUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 2:15 AM
 * To change this template use File | Settings | File Templates.
 */
public class BatchletManager extends Command<BatchletPayLoad, SimpleResponse, AvailableBatchlets> {

    @Override
    protected AvailableBatchlets status(User user) throws Exception {
        List<Batchlet> batchlets = ObjectifyService.ofy().load().type(Batchlet.class).filter("user", user).list();
        AvailableBatchlets availableBatchlets = new AvailableBatchlets();
        for (Batchlet batchlet : batchlets) {
            BatchletPayLoad batchletPayLoad = new BatchletPayLoad();
            batchletPayLoad.setBatchletClass(batchlet.getClassName());
            batchletPayLoad.setFrequency(batchlet.getFrequency());
            batchletPayLoad.setFrequencyTimeUnit(TimeUnit.valueOf(batchlet.getFrequenceTimeUnit()));
            availableBatchlets.getRegisterdBatchlets().add(batchletPayLoad);
        }
        return availableBatchlets;
    }

    @Override
    protected SimpleResponse execute(BatchletPayLoad input, User user, String fromIpAddress) throws Exception {
        String id = SignUtils.calculateSha1Digest(user.getEmail() + input.getBatchletClass());
        Batchlet batchlet = new Batchlet();
        batchlet.setId(id);
        batchlet.setFrequency(input.getFrequency());
        batchlet.setClassName(input.getBatchletClass());
        batchlet.setFrequenceTimeUnit(input.getFrequencyTimeUnit().name());
        batchlet.setUser(user);
        ObjectifyService.ofy().save().entity(batchlet).now();
        return new SimpleResponse(true);
    }

    @Override
    protected Class<BatchletPayLoad> getInputObjectType() {
        return BatchletPayLoad.class;
    }

    @Override
    protected Class<SimpleResponse> getOutputObjectType() {
        return SimpleResponse.class;
    }

    @Override
    public boolean isPublic() {
        return false;
    }
}
