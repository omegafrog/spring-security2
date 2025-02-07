package org.example.springsecurity.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.example.springsecurity.global.dto.RsData;
import org.springframework.stereotype.Component;

@Component
public class Util {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public class Json{

        public static <T> String toJson(RsData<T> data)  {
            try {
                return objectMapper.writeValueAsString(data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
