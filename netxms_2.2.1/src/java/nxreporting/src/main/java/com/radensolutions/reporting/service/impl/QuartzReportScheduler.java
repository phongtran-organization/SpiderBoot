package com.radensolutions.reporting.service.impl;

import com.radensolutions.reporting.job.GeneratorJob;
import com.radensolutions.reporting.service.NotificationService;
import com.radensolutions.reporting.service.ReportScheduler;
import com.radensolutions.reporting.service.Session;
import org.netxms.client.SessionNotification;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.*;
import java.util.Calendar;

@Service("reportScheduler")
public class QuartzReportScheduler implements ReportScheduler {
    public static final String SYSTEM_USER_NAME = "SYSTEM";
    public static final int TYPE_ONCE = 0;
    public static final int TYPE_DAILY = 1;
    public static final int TYPE_WEEKLY = 2;
    public static final int TYPE_MONTHLY = 3;
    private static final Logger log = LoggerFactory.getLogger(QuartzReportScheduler.class);
    public static String[] dayOfWeekNames = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private Session session;
    @Autowired
    private Scheduler scheduler;

    @Override
    public UUID execute(UUID jobId, int userId, UUID reportUuid, Map<String, Object> parameters) {
        if (jobId == null) {
            jobId = UUID.randomUUID();
        }
        log.debug("New job " + jobId + " for report " + reportUuid);

        String groupName = reportUuid.toString();

        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setName(jobId.toString());
        jobDetailFactory.setGroup(groupName);
        jobDetailFactory.setJobClass(GeneratorJob.class);
        jobDetailFactory.afterPropertiesSet();
        JobDetail jobDetail = jobDetailFactory.getObject();

        final JobDataMap dataMap = jobDetail.getJobDataMap();
        dataMap.put("jobId", jobId);
        dataMap.put("reportId", reportUuid);
        dataMap.put("parameters", parameters);
        dataMap.put("userId", userId);

        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setName(jobId.toString() + "_trigger");
        triggerFactoryBean.setGroup(groupName);
        triggerFactoryBean.setJobDetail(jobDetail);
        triggerFactoryBean.setRepeatCount(0);
        triggerFactoryBean.afterPropertiesSet();

        Trigger trigger = triggerFactoryBean.getObject();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            log.error("Can't schedule job", e);
            jobId = null;
        }
        return jobId;
    }

    @Override
    public UUID addRecurrent(UUID jobId, UUID reportUuid, int jobType, int daysOfWeek, int daysOfMonth, Date startTime,
                             Map<String, Object> parameters, int userId) {
        Calendar dateTime = Calendar.getInstance();
        dateTime.setTime(startTime);

        int hours = dateTime.get(Calendar.HOUR_OF_DAY);
        int minutes = dateTime.get(Calendar.MINUTE);
        log.debug(String.format("reportId=%1$s, hours=%2$d, minutes=%3$d, dow=%4$d, dom=%5$d", reportUuid, hours, minutes, daysOfWeek, daysOfMonth));

        if (jobId == null) {
            jobId = UUID.randomUUID();
        }
        // TODO: Remove old jobs and triggers
        // TODO: check for running jobs
        final String reportKey = reportUuid.toString();

        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(GeneratorJob.class);
        jobDetailFactory.setName(jobId.toString());
        jobDetailFactory.setGroup(reportKey);
        jobDetailFactory.afterPropertiesSet();
        JobDetail job = jobDetailFactory.getObject();
        final JobDataMap dataMap = job.getJobDataMap();
        dataMap.put("jobId", jobId);
        dataMap.put("jobType", jobType);
        dataMap.put("reportId", reportUuid);
        dataMap.put("userId", userId);
        dataMap.put("parameters", parameters);
        dataMap.put("startDate", startTime);
        dataMap.put("daysOfWeek", daysOfWeek);
        dataMap.put("daysOfMonth", daysOfMonth);

        Trigger trigger;
        if (jobType == TYPE_ONCE) {
            SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
            triggerFactoryBean.setName(jobId.toString() + "_trigger");
            triggerFactoryBean.setGroup(reportKey);
            triggerFactoryBean.setJobDetail(job);
            triggerFactoryBean.setRepeatCount(0);
            triggerFactoryBean.setStartTime(startTime);
            triggerFactoryBean.afterPropertiesSet();
            trigger = triggerFactoryBean.getObject();
        } else {
            String cronExpression = null;
            switch (jobType) {
                case TYPE_DAILY:
                    cronExpression = "0 " + minutes + " " + hours + " * * ?";
                    break;
                case TYPE_WEEKLY:
                    String sDays = "";
                    for (int i = 0; i < 7; i++) {
                        if (((daysOfWeek >> i) & 0x01) != 0) {
                            sDays += (sDays.length() > 0 ? "," : "") + dayOfWeekNames[(7 - i) - 1];
                        }
                    }
                    if (sDays.length() > 0) {
                        cronExpression = "0 " + minutes + " " + hours + " ? * " + sDays;
                    }
                    break;
                case TYPE_MONTHLY:
                    String sDayOfM = "";
                    for (int i = 0; i < 31; i++) {
                        if (((daysOfMonth >> i) & 0x01) != 0)
                            sDayOfM += (sDayOfM.length() > 0 ? "," : "") + String.valueOf(31 - i);
                    }
                    if (sDayOfM.length() > 0) {
                        cronExpression = "0 " + minutes + " " + hours + " " + sDayOfM + " * ?";
                    }
                    break;
            }

            CronTriggerFactoryBean cronTriggerFactoryBean = new CronTriggerFactoryBean();
            cronTriggerFactoryBean.setJobDetail(job);
            cronTriggerFactoryBean.setName(jobId.toString() + "_trigger");
            cronTriggerFactoryBean.setGroup(reportKey);
            cronTriggerFactoryBean.setCronExpression(cronExpression);
            try {
                cronTriggerFactoryBean.afterPropertiesSet();
            } catch (ParseException e) {
                log.error("Can't parse cron expression " + cronExpression, e);
            }

            trigger = cronTriggerFactoryBean.getObject();
        }

        try {
            scheduler.getListenerManager()
                    .addJobListener(new ReportingJobListener(jobId, jobType), KeyMatcher.keyEquals(job.getKey()));
            scheduler.scheduleJob(job, trigger);
        } catch (SchedulerException e) {
            log.error("Can't schedule job", e);
            jobId = null;
        }
        return jobId;
    }

    public List<JobDetail> getSchedules(UUID reportUuid) {
        List<JobDetail> jobDetailsList = new ArrayList<JobDetail>(0);
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    jobDetailsList.add(scheduler.getJobDetail(jobKey));
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        return jobDetailsList;
    }

    @Override
    public boolean deleteScheduleJob(UUID reportId, UUID jobId) {
        try {
            final Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(reportId.toString()));
            for (JobKey key : jobKeys) {
                if (key.getName().equalsIgnoreCase(jobId.toString()))
                    scheduler.deleteJob(key);
            }
        } catch (SchedulerException e) {
            session.sendNotify(SessionNotification.RS_SCHEDULES_MODIFIED, 0);
            notificationService.delete(jobId);
            log.error("Can't find or remove old triggers", e);
            return false;
        }
        return true;
    }

    public class ReportingJobListener implements JobListener {
        UUID jobId;
        int jobType;

        public ReportingJobListener(UUID jobId, int jobType) {
            this.jobId = jobId;
            this.jobType = jobType;
        }

        @Override
        public String getName() {
            return "reportingJobListener";
        }

        @Override
        public void jobToBeExecuted(JobExecutionContext context) {
        }

        @Override
        public void jobExecutionVetoed(JobExecutionContext context) {
        }

        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            if (jobType == TYPE_ONCE)
                notificationService.delete(jobId);
        }

    }
}
