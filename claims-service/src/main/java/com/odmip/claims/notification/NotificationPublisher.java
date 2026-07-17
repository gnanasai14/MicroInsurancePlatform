package com.odmip.claims.notification;

import com.odmip.claims.entity.NotificationAudit;
import com.odmip.claims.repository.NotificationAuditRepository;
import com.odmip.common.event.ClaimStatusChangedEvent;
import com.odmip.common.event.FraudFlaggedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    private final NotificationAuditRepository auditRepository;

    @Value("${odmip.sns.enabled:false}")
    private boolean snsEnabled;

    @Value("${odmip.sns.claim-events-topic-arn:arn:aws:sns:us-east-1:123456789012:claims-topic}")
    private String claimEventsTopicArn;

    @Value("${odmip.sns.fraud-events-topic-arn:arn:aws:sns:us-east-1:123456789012:fraud-topic}")
    private String fraudEventsTopicArn;

    @Value("${odmip.sns.region:us-east-1}")
    private String region;

    public NotificationPublisher(NotificationAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void publishClaimStatusChanged(ClaimStatusChangedEvent event) {
        publish(claimEventsTopicArn, event, "ClaimStatusChangedEvent", event.claimId());
    }

    public void publishFraudFlagged(FraudFlaggedEvent event) {
        publish(fraudEventsTopicArn, event, "FraudFlaggedEvent", event.claimId());
    }

    private void publish(String topicArn, Object event, String eventName, Long claimId) {
        String json = "";
        try {
            json = MAPPER.writeValueAsString(event);
        } catch (Exception ex) {
            log.error("Failed to serialize event: {}", ex.getMessage());
            return;
        }

        String messageId = null;
        String status = "MOCKED";

        if (!snsEnabled) {
            log.info("[SNS-disabled] Would publish {} to {}: {}", eventName, topicArn, json);
            messageId = "MOCK-" + System.currentTimeMillis();
        } else {
            try (SnsClient sns = SnsClient.builder().region(Region.of(region)).build()) {
                PublishResponse res = sns.publish(PublishRequest.builder().topicArn(topicArn).message(json).build());
                messageId = res.messageId();
                status = "SENT";
                log.info("Published {} to SNS topic {}", eventName, topicArn);
            } catch (Exception ex) {
                log.error("Failed to publish {} to SNS: {}", eventName, ex.getMessage());
                status = "FAILED";
                messageId = "ERROR-" + System.currentTimeMillis();
            }
        }

        try {
            // Record audit log entry in DB
            auditRepository.save(NotificationAudit.builder()
                    .claimId(claimId)
                    .eventType(eventName)
                    .topicArn(topicArn)
                    .messageId(messageId)
                    .status(status)
                    .build());
        } catch (Exception dbEx) {
            log.error("Failed to save notification audit log to DB: {}", dbEx.getMessage());
        }
    }
}
