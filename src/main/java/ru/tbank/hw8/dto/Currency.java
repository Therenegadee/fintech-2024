package ru.tbank.hw8.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JacksonXmlRootElement(localName = "Item")
public class Currency {
    @JacksonXmlProperty(localName = "ID", isAttribute = true)
    private String id;

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name")
    private String name;

    @JsonProperty("ISO_Char_Code")
    @JacksonXmlProperty(localName = "ISO_Char_Code")
    private String code;

    @JsonProperty("ISO_Num_Code")
    @JacksonXmlProperty(localName = "ISO_Num_Code")
    private int numericCode;

    @JsonProperty("EngName")
    @JacksonXmlProperty(localName = "EngName")
    private String englishName;

    @JsonProperty("Nominal")
    @JacksonXmlProperty(localName = "Nominal")
    private int nominalValue;

    @JsonProperty("ParentCode")
    @JacksonXmlProperty(localName = "ParentCode")
    private String parentCode;
}
