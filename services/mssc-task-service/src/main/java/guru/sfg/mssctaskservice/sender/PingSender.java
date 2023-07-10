package guru.sfg.mssctaskservice.sender;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.mssctaskservice.config.JmsConfig;
import guru.sfg.mssctaskservice.model.PingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PingSender {

  private final JmsTemplate jmsTemplate;
  private final ObjectMapper objectMapper;

  @Scheduled(fixedRate = 60000)
  public void sendMessage() {
    PingMessage message = PingMessage
        .builder()
        .id(UUID.randomUUID())
        .message("ping message")
        .build();

    jmsTemplate.convertAndSend(JmsConfig.PING_QUEUE, message);
    log.debug("[SENDER PING] Message {} sent", message.getId());
  }

  @Scheduled(fixedRate = 60000)
  public void sendAndReceiveMessage() throws JMSException, IOException {
    PingMessage message = PingMessage
        .builder()
        .id(UUID.randomUUID())
        .message("PING")
        .build();
    log.debug("[SENDER PING PONG] Sending {} ", objectMapper.writeValueAsString(message));

    Message receivedMessage = jmsTemplate.sendAndReceive(JmsConfig.PING_PONG_QUEUE, session -> {
      Message pingPongMessage;
      try {
        pingPongMessage = session.createTextMessage(objectMapper.writeValueAsString(message));
        pingPongMessage.setStringProperty("_type", "guru.sfg.mssctaskservice.model.PingMessage");

        return pingPongMessage;
      } catch (JsonProcessingException e) {
        throw new JMSException("Message delivery or reception failed");
      }
    });

    assert receivedMessage != null;
    log.debug("[SENDER PING PONG] Received {}", receivedMessage.getBody(String.class));
  }
}
