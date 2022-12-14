package org.example.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDtoRq {
    private String roomName;
    private LocalDate startDate;
    private LocalDate endDate;
    private CustomerDtoRq customer;
}
