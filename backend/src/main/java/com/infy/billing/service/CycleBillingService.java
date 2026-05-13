package com.infy.billing.service;

public interface CycleBillingService {
    
    class JobStats {
        public int totalRecords = 0;
        public int successCount = 0;
        public int failureCount = 0;
        public StringBuilder errorSummary = new StringBuilder();
    }

    JobStats processCycleBilling();
    JobStats processDunningRetries();
}
