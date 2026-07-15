package com.odmip.claims.notification;

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

/**
 * Real-Time Alerts (SNS-based notifications).
 *
 * odmip.sns.enabled=false (default, see application.yml) -> logs the event
 * instead of calling AWS, so this runs with zero AWS setup for week-1/2 dev
 * and demos. Flip the flag + fill in the topic ARNs once the team provisions
 * SNS topics, and real publishing kicks in with no code changes needed
 * elsewhere in claims-service.
 */
@Component
public class NotificationPublisher {

    private static final Logger log = LoggerFactory.getLogger(NotificationPublisher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Value("${odmip.sns.enabled}")
    private boolean snsEnabled;

    @Value("${odmip.sns.claim-events-topic-arn}")
    private String claimEventsTopicArn;

    @Value("${odmip.sns.fraud-events-topic-arn}")
    private String fraudEventsTopicArn;

    @Value("${odmip.sns.region}")
    private String region;

    public void publishClaimStatusChanged(ClaimStatusChangedEvent event) {
        publish(claimEventsTopicArn, event, "ClaimStatusChangedEvent");
    }

    public void publishFraudFlagged(FraudFlaggedEvent event) {
        publish(fraudEventsTopicArn, event, "FraudFlaggedEvent");
    }

    private void publish(String topicArn, Object event, String eventName) {
        try {
            String json = MAPPER.writeValueAsString(event);
            if (!snsEnabled) {
                log.info("[SNS-disabled] Would publish {} to {}: {}", eventName, topicArn, json);
                return;
            }
            try (SnsClient sns = SnsClient.builder().region(Region.of(region)).build()) {
                sns.publish(PublishRequest.builder().topicArn(topicArn).message(json).build());
                log.info("Published {} to {}", eventName, topicArn);
            }
        } catch (Exception ex) {
            // Never let a notification failure break the claims workflow itself.
            log.error("Failed to publish {}: {}", eventName, ex.getMessage());
        }
    }
}
