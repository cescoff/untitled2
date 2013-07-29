package fr.untitled2.android.sqlilite;

import fr.untitled2.android.sqlilite.DbHelper;
import fr.untitled2.common.entities.LogRecording;

/**
 * Created with IntelliJ IDEA.
 * User: corentinescoffier
 * Date: 3/11/13
 * Time: 8:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class LogRecordingWithStatus {
    
    private LogRecording logRecording;
    
    private DbHelper.LogStatus status;

    public LogRecordingWithStatus(LogRecording logRecording, DbHelper.LogStatus status) {
        this.logRecording = logRecording;
        this.status = status;
    }

    public LogRecording getLogRecording() {
        return logRecording;
    }

    public DbHelper.LogStatus getStatus() {
        return status;
    }
}
