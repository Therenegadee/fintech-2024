package ru.tbank;

import lombok.extern.slf4j.Slf4j;
import ru.tbank.model.City;
import ru.tbank.util.JsonConverter;
import ru.tbank.util.XmlConverter;

import java.io.File;
import java.util.Objects;

@Slf4j
public class Main {

    public static void main(String[] args) {
        log.info("Начало работы приложения.");
        File correctFile = new File("./src/main/resources/hw2/city.json");
        File invalidFile = new File("./src/main/resources/hw2/city-error.json");

        log.info("Начало конвертации корректного JSON файла в объект Город.");
        City correctCity = JsonConverter.convertJsonToPojo(correctFile, City.class);
        if (Objects.nonNull(correctCity)) {
            log.debug("Результат считывания корректного файла: {}", correctCity);
        } else {
            String errorMessage = "Результат десериализации корректного файла вернул null!";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.info("Начало считывания файла с ошибкой.");
        City invalidCity = JsonConverter.convertJsonToPojo(invalidFile, City.class);
        if (Objects.isNull(invalidCity)) {
            log.debug("Результат десериализации некорректного файла вернул null.");
        } else {
            String errorMessage = "Результат десериализации некорректного файла вернул объект!";
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        log.info("Начало конвертации объекта Город (slug = {}) в XML файл.", correctCity.getSlug());
        File file = XmlConverter.convertPojoToXml(correctCity);
        if (Objects.nonNull(file)) {
            log.info("Объект Город (slug = {}) был успешно сконвертирован в XML файл. Путь к файлу: {}.", correctCity.getSlug(),
                    file.getPath());
        } else {
            String errorMessage = String.format("Произошла ошибка при конвертации объекта Город (slug = %s) в XML файл и он не был сохранен!", correctCity.getSlug());
            log.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }
}