package ru.apolyakov.example.service.calls;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DisallowConcurrentExecution
public class ProceedCallsJob implements Job {
    private static final Logger log = LoggerFactory.getLogger(ProceedCallsJob.class);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        ProceedCallsServiceImpl proceedCallsService = (ProceedCallsServiceImpl) jobDataMap.get("proceedCallsService");
        proceedCallsService.proceedCalls();
    }
}
