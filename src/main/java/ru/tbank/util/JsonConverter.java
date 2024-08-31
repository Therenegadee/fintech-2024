package ru.tbank.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
public class JsonConverter {

    public static <T> T convertJsonToPojo(File file, Class<T> pojoClass) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            log.debug("Начало конвертации JSON файла с названием {} в POJO объект с названием класса: {}.",
                    file.getName(), pojoClass.getSimpleName());
            JsonNode cityNode = objectMapper.readTree(file);
            var pojo = objectMapper.treeToValue(cityNode, pojoClass);
            log.debug("JSON Файл {} был успешно сконвертирован в POJO объект ({}).", file.getName(), pojoClass.getSimpleName());
            return pojo;
        } catch (IOException e) {
            log.error("В процессе конвертации JSON файла в POJO объект класса {} произошла ошибка и он не был десериализован.",
                    pojoClass.getSimpleName(), e);
            return null;
        }
    }
}
