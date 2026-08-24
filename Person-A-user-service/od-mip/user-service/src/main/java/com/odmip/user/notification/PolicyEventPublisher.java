package com.odmip.user.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.odmip.common.event.PolicyExpiringEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

@Component
public class PolicyEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PolicyEventPublisher.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    @Value("${odmip.sns.enabled:true}")
    private boolean snsEnabled;

    @Value("${odmip.sns.policy-events-topic-arn:arn:aws:sns:us-east-1:000000000000:odmip-policy-events}")
    private String policyEventsTopicArn;

    @Value("${odmip.sns.region:us-east-1}")
    private String region;

    public void publishPolicyExpiring(PolicyExpiringEvent event) {
        String json = "";
        try {
            json = MAPPER.writeValueAsString(event);
        } catch (Exception ex) {
            log.error("Failed to serialize event: {}", ex.getMessage());
            return;
        }

        if (!snsEnabled) {
            log.info("[SNS-disabled] Would publish PolicyExpiringEvent to {}: {}", policyEventsTopicArn, json);
        } else {
            try (SnsClient sns = SnsClient.builder().region(Region.of(region)).build()) {
                PublishResponse res = sns.publish(PublishRequest.builder()
                        .topicArn(policyEventsTopicArn)
                        .message(json)
                        .build());
                log.info("Published PolicyExpiringEvent (messageId={}) to SNS topic {}", res.messageId(), policyEventsTopicArn);
            } catch (Exception ex) {
                log.error("Failed to publish PolicyExpiringEvent to SNS: {}", ex.getMessage());
            }
        }
    }
}
