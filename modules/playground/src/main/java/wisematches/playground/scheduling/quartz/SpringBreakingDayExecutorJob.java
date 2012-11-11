package wisematches.playground.scheduling.quartz;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.springframework.context.ApplicationContext;
import wisematches.playground.scheduling.BreakingDayListener;

import java.util.Date;
import java.util.Map;

/**
 * @author Sergey Klimenko (smklimenko@gmail.com)
 */
public class SpringBreakingDayExecutorJob implements Job {
	private static final Log log = LogFactory.getLog("wisematches.server.playground.scheduling.quartz");

	public SpringBreakingDayExecutorJob() {
	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		final Date scheduledFireTime = context.getScheduledFireTime();
		ApplicationContext applicationContext = (ApplicationContext) context.get("SpringApplicationContext");
		if (applicationContext == null) {
			try {
				applicationContext = (ApplicationContext) context.getScheduler().getContext().get("SpringApplicationContext");
			} catch (SchedulerException ex) {
				log.error("Scheduler context can't be received", ex);
			}
		}


		if (applicationContext != null) {
			final Map<String, BreakingDayListener> listenerMap = applicationContext.getBeansOfType(BreakingDayListener.class);
			for (Map.Entry<String, BreakingDayListener> entry : listenerMap.entrySet()) {
				entry.getValue().breakingDayTime(scheduledFireTime);
			}
		} else {
			log.error("No application context for SpringBreakingDayExecutorJob");
		}
	}
}