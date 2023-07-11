package guru.sfg.msscbeerservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import guru.sfg.msscbeerservice.events.AddInventoryEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmsConfig {

  public static final String BREWING_REQUEST_QUEUE = "brewing-request";
  public static final String ADD_INVENTORY_QUEUE = "add-inventory";

  @Bean
  public MessageConverter jacksonJmsMessageConverter(ObjectMapper objectMapper) {
    Map<String, Class<?>> typeMap = new HashMap<>();
    typeMap.put("guru.sfg.common.events.AddInventoryEvent", AddInventoryEvent.class);

    MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
    converter.setTargetType(MessageType.TEXT);
    converter.setTypeIdPropertyName("_type");
    converter.setObjectMapper(objectMapper);
    converter.setTypeIdMappings(typeMap);

    return converter;
  }
}
