/*
 * @author <a href="mailto:kisg@mailbox.hu">Gergely Kis</a>
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id: TimerService.java,v 1.1 2007/08/24 17:24:55 mem61 Exp $
 */
package org.gridlab.gridsphere.services.core.timer;

import java.util.Date;
import java.util.TimerTask;

/**
 * Service to perform tasks at a given time or periodically.
 */
public interface TimerService {

    void schedule(String timerId, TimerTask task, Date time);

    void schedule(String timerId, TimerTask task, long delay, long period);

    void schedule(String timerId, TimerTask task, Date firstTime, long period);

    void scheduleAtFixedRate(String timerId, TimerTask task, long delay, long period);

    void scheduleAtFixedRate(String timerId, TimerTask task, Date firstTime, long period);

    void cancel(String timerId);

}
