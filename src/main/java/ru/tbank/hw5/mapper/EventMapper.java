package ru.tbank.hw5.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import ru.tbank.hw5.dto.Event;
import ru.tbank.hw5.dto.KudaGoEventResponse;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface EventMapper {

    @Mapping(target = "price", expression = "java(dto.isFree() ? (double) 0 : parsePrice(dto.getPrice()))")
    Event toEvent(KudaGoEventResponse.EventDTO dto);

    @Named("parsePrice")
    default Double parsePrice(String price) {
        if (Objects.isNull(price) || price.isEmpty()) {
            return (double) 0;
        }

        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(price);
        double minPrice = Double.MAX_VALUE;
        while (matcher.find()) {
            double currentPrice = Double.parseDouble(matcher.group());
            if (currentPrice < minPrice) {
                minPrice = currentPrice;
            }
        }

        return minPrice == Double.MAX_VALUE ? 0L : minPrice;
    }
}
