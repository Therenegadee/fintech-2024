package ru.tbank.util;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.UUID;

@Slf4j
public class XmlConverter {
    public static <T> File convertPojoToXml(T pojo) {
        try {
            log.debug("Начало конвертации POJO объекта (класс {}) в XML файл.", pojo.getClass().getSimpleName());
            XmlMapper xmlMapper = new XmlMapper();
            String pathToFile = "./src/main/resources/hw2/";
            String fileName = String.format("%s-%s.xml", pojo.getClass().getSimpleName(),
                    UUID.randomUUID());
            log.debug("Название будущего XML файла: {}. Путь к сохранению файла: {}.", fileName, pathToFile);
            File file = new File(pathToFile + fileName);
            xmlMapper.writeValue(file, pojo);
            log.debug("POJO объект (класс {}) был успешно сконвертирован в XML файл (находится по пути: {}).",
                    pojo.getClass().getSimpleName(), pathToFile + fileName);
            return file;
        } catch (Exception e) {
            log.warn("В процессе конвертации POJO объекта (класс {}) в XML файл произошла ошибка и объект не был сконвертирован.",
                    pojo.getClass().getSimpleName(), e);
            return null;
        }
    }
}
