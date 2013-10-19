package fr.untitled2.entities;

import com.googlecode.objectify.Key;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.*;
import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDateTime;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 10/13/13
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity @Cache
public class BatchTask {

    @Id
    private String id;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime startDate = LocalDateTime.now();

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime lastReadDate;

    @Translate(LocalDateTimeTranslatorFactory.class)
    private LocalDateTime endDate;

    private String inputJson;

    private String outputJson;

    @Index
    private Key<User> user;

    private String log;

    @Index
    private Key<BatchServer> processingServer;

    @Index
    private Key<BatchServer> fromServer;

    @Index
    private Key<Batchlet> batchlet;

    private boolean success;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(LocalDateTime lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getInputJson() {
        return inputJson;
    }

    public void setInputJson(String inputJson) {
        this.inputJson = inputJson;
    }

    public String getOutputJson() {
        return outputJson;
    }

    public void setOutputJson(String outputJson) {
        this.outputJson = outputJson;
    }

    public User getUser() {
        return ObjectifyService.ofy().load().key(user).get();
    }

    public void setUser(User user) {
        this.user = Key.create(User.class, user.getUserId());
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public BatchServer getProcessingServer() {
        if (processingServer != null) return ObjectifyService.ofy().load().key(processingServer).get();
        return null;
    }

    public void setProcessingServer(BatchServer processingServer) {
        this.processingServer = Key.create(BatchServer.class, processingServer.getServerId());
    }

    public BatchServer getFromServer() {
        return ObjectifyService.ofy().load().key(fromServer).get();
    }

    public void setFromServer(BatchServer fromServer) {
        this.fromServer = Key.create(BatchServer.class, fromServer.getServerId());
    }

    public Batchlet getBatchlet() {
        return ObjectifyService.ofy().load().key(batchlet).get();
    }

    public void setBatchlet(Batchlet batchlet) {
        this.batchlet = Key.create(Batchlet.class, batchlet.getId());
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BatchTask batchTask = (BatchTask) o;

        if (success != batchTask.success) return false;
        if (batchlet != null ? !batchlet.equals(batchTask.batchlet) : batchTask.batchlet != null) return false;
        if (endDate != null ? !endDate.equals(batchTask.endDate) : batchTask.endDate != null) return false;
        if (fromServer != null ? !fromServer.equals(batchTask.fromServer) : batchTask.fromServer != null) return false;
        if (id != null ? !id.equals(batchTask.id) : batchTask.id != null) return false;
        if (inputJson != null ? !inputJson.equals(batchTask.inputJson) : batchTask.inputJson != null) return false;
        if (lastReadDate != null ? !lastReadDate.equals(batchTask.lastReadDate) : batchTask.lastReadDate != null)
            return false;
        if (log != null ? !log.equals(batchTask.log) : batchTask.log != null) return false;
        if (outputJson != null ? !outputJson.equals(batchTask.outputJson) : batchTask.outputJson != null) return false;
        if (processingServer != null ? !processingServer.equals(batchTask.processingServer) : batchTask.processingServer != null)
            return false;
        if (startDate != null ? !startDate.equals(batchTask.startDate) : batchTask.startDate != null) return false;
        if (user != null ? !user.equals(batchTask.user) : batchTask.user != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (lastReadDate != null ? lastReadDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (inputJson != null ? inputJson.hashCode() : 0);
        result = 31 * result + (outputJson != null ? outputJson.hashCode() : 0);
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (log != null ? log.hashCode() : 0);
        result = 31 * result + (processingServer != null ? processingServer.hashCode() : 0);
        result = 31 * result + (fromServer != null ? fromServer.hashCode() : 0);
        result = 31 * result + (batchlet != null ? batchlet.hashCode() : 0);
        result = 31 * result + (success ? 1 : 0);
        return result;
    }

    public boolean isDone() {
        return success || StringUtils.isNotEmpty(outputJson) || StringUtils.isNotEmpty(log);
    }

}
