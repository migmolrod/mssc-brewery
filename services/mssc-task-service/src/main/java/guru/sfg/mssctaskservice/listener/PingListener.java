package guru.sfg.mssctaskservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.mssctaskservice.config.JmsConfig;
import guru.sfg.mssctaskservice.model.PingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import javax.jms.Message;

@Component
@RequiredArgsConstructor
@Slf4j
public class PingListener {

  private final JmsTemplate jmsTemplate;
  private final ObjectMapper objectMapper;

  @JmsListener(destination = JmsConfig.PING_QUEUE)
  public void listen(@Payload PingMessage pingMessage,
                     @Headers MessageHeaders headers,
                     Message message) throws JsonProcessingException {
    log.debug("[LISTENER PING] Received {}. Headers {}. Message {}",
        objectMapper.writeValueAsString(pingMessage),
        headers,
        message);
  }

  @JmsListener(destination = JmsConfig.PING_PONG_QUEUE)
  public void listenForReply(@Payload PingMessage pingMessage,
                             @Headers MessageHeaders headers,
                             Message message) throws Exception {
    PingMessage reply = PingMessage
        .builder()
        .id(pingMessage.getId())
        .message("PONG")
        .build();
    jmsTemplate.convertAndSend(message.getJMSReplyTo(), reply);

    log.debug("[LISTENER PING PONG] Received {}. Headers {}. Message {}",
        objectMapper.writeValueAsString(pingMessage),
        headers,
        message);
    log.debug("[LISTENER PING PONG] Sending reply {}",
        objectMapper.writeValueAsString(reply));
  }
}
