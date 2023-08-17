package guru.sfg.beer.order.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

@Configuration
public class JmsConfig {

  public final static String VALIDATE_ORDER_QUEUE = "validate-order";
  public final static String VALIDATE_ORDER_RESPONSE_QUEUE = "validate-order-response";
  public final static String ALLOCATE_ORDER_QUEUE = "allocate-order";
  public final static String ALLOCATE_ORDER_RESPONSE_QUEUE = "allocate-order-response";
  public final static String ALLOCATE_FAILURE_QUEUE = "allocate-failure";
  public final static String DEALLOCATE_ORDER_QUEUE = "deallocate-order";

  @Bean
  public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    converter.setObjectMapper(objectMapper);

    return converter;
  }
}
