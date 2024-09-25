package ru.tbank.hw5.dto;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Place {
    private int id;
    private String title;
    private String slug;
    private String address;
    private String phone;
    private String siteUrl;
    private String subway;
    private boolean isClosed;
    private String location;
    private boolean hasParkingLot;
}
