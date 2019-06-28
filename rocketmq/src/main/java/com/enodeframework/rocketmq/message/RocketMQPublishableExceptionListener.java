package com.enodeframework.rocketmq.message;

import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import com.alibaba.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import com.alibaba.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import com.alibaba.rocketmq.common.message.MessageExt;
import com.enodeframework.common.serializing.JsonTool;
import com.enodeframework.queue.QueueMessage;
import com.enodeframework.queue.publishableexceptions.AbstractPublishableExceptionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author anruence@gmail.com
 */
public class RocketMQPublishableExceptionListener extends AbstractPublishableExceptionListener implements MessageListenerConcurrently {

    private static Logger logger = LoggerFactory.getLogger(RocketMQPublishableExceptionListener.class);

    @Override
    public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs, ConsumeConcurrentlyContext context) {
        try {
            QueueMessage queueMessage = RocketMQTool.covertToQueueMessage(msgs);
            handle(queueMessage, message -> {
            });
        } catch (Exception e) {
            logger.error("Ops, consume PublishableExceptionMessage failed, msgs:{}", JsonTool.serialize(msgs), e);
            return ConsumeConcurrentlyStatus.RECONSUME_LATER;
        }
        return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
    }
}