package ru.apolyakov.example.service.calls;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Aggregate data for all calls per day
 */
@DisallowConcurrentExecution
public class ProceedBatchCallsJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(ProceedBatchCallsJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        ProceedCallsServiceImpl proceedCallsService = (ProceedCallsServiceImpl) jobDataMap.get("proceedCallsService");
        proceedCallsService.proceedCalls();
    }
}
