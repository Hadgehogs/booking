package org.example.booking.controller;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.json.JsonMapper;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.SneakyThrows;
import org.example.booking.dto.BookingDtoRq;
import org.example.booking.dto.BookingDtoRs;
import org.example.booking.service.BookingService;
import org.example.booking.service.CustomerService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookingControllerTest {
    @LocalServerPort
    private int portNum;

    @PostConstruct
    public void init() {
        RestAssured.baseURI = "http://localhost:" + portNum;
    }

    @MockBean
    CustomerService customerService;

    @MockBean
    BookingService bookingService;

    // Тесты метода getBookingsByCustomer
    @Test
    //Сначало потестим получение брони по почте
    public void getBookingsByCustomerMail() {

        String targetMail = "111@gmail.com";
        List<BookingDtoRs> expected = new ArrayList<BookingDtoRs>();
        BookingDtoRs targetBooking = BookingDtoRs.builder()
                .number("111")
                .roomName("111")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 1, 2))
                .customerName("Petr")
                .build();
        expected.add(targetBooking);

        Mockito.when(customerService.getBookingsByEmail(targetMail)).thenReturn(expected);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .param("customerEmail", "222@gmail.com")
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();
        TypeRef<List<BookingDtoRs>> typeRef = new TypeRef<List<BookingDtoRs>>() {
        };
        Assertions.assertEquals(response.statusCode(), HttpStatus.OK.value());
        List<BookingDtoRs> actual = response.body().as(typeRef);
        Assertions.assertEquals(expected, actual);

    }

    @Test
    //Теперь потестим получение брони по ошибочной почте
    public void getBookingsByCustomerMailWithError() {

        String targetMail = "111@gmail.com";
        Mockito.when(customerService.getBookingsByEmail(targetMail)).thenThrow(new NoSuchElementException("No value present"));

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .param("customerEmail", targetMail)
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();
        Assertions.assertEquals(response.statusCode(), HttpStatus.BAD_REQUEST.value());
    }

    @Test
    //Затем потестим получение брони по номеру
    public void getBookingsByCustomerNumber() {
        String targetNumber = "111";
        BookingDtoRs expected = BookingDtoRs.builder()
                .number(targetNumber)
                .roomName("111")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 1, 2))
                .customerName("Petr")
                .build();

        Mockito.when(bookingService.getBookingByNumber(targetNumber)).thenReturn(Optional.of(expected));

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .param("number", targetNumber)
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();
        Assertions.assertEquals(response.statusCode(), HttpStatus.OK.value());
        BookingDtoRs actual = response.body().as(BookingDtoRs.class);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    //Затем потестим получение брони по неверному номеру
    public void getBookingsByCustomerNumberWithError() {

        String targetNumber = "111";
        BookingDtoRs expected = BookingDtoRs.builder()
                .number(targetNumber)
                .roomName("111")
                .startDate(LocalDate.of(2000, 1, 1))
                .endDate(LocalDate.of(2000, 1, 2))
                .customerName("Petr")
                .build();

        Mockito.when(bookingService.getBookingByNumber(targetNumber)).thenThrow(new NoSuchElementException("No value present"));

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .param("number", targetNumber)
                .when()
                .get("/booking")
                .then()
                .extract()
                .response();
        Assertions.assertEquals(response.statusCode(), HttpStatus.BAD_REQUEST.value());
    }

    // тесты метода createBooking

    @Test
    @SneakyThrows
    //Сначало потестим успешное создание брони
    public void createBooking() {
        String welcomeToTheClubBaddy = "{\n" +
                "\t\"number\": \"111\",\n" +
                "\t\"roomName\" : \"111\",\t\n" +
                "\t\"startDate\": \"2012-04-21T00:00:00\",\n" +
                "\t\"endDate\": \"2012-04-23T00:00:00\",\t\n" +
                "\t\"customer\":{ \"email\":\"222@gmail.com\",\"name\":\"Petr\"}\n" +
                "}\n";
        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader reader = mapper.readerFor(BookingDtoRq.class);
        BookingDtoRq bookingDtoRq = reader.readValue(welcomeToTheClubBaddy);

        String expected = "SPQR";
        Mockito.when(bookingService.createBooking(bookingDtoRq)).thenReturn(expected);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(welcomeToTheClubBaddy)
                .when()
                .post("/booking")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(response.statusCode(), HttpStatus.OK.value());
        String actual = response.body().asString();
        Assertions.assertEquals(expected, actual);

    }

    @Test
    @SneakyThrows
    //Теперь неуспешное создание брони
    public void createBookingWithError() {
        String welcomeToTheClubBaddy = "{\n" +
                "\t\"number\": \"111\",\n" +
                "\t\"roomName\" : \"111\",\t\n" +
                "\t\"startDate\": \"2012-04-21T00:00:00\",\n" +
                "\t\"endDate\": \"2012-04-23T00:00:00\",\t\n" +
                "\t\"customer\":{ \"email\":\"222@gmail.com\",\"name\":\"Petr\"}\n" +
                "}\n";
        ObjectMapper mapper = JsonMapper.builder()
                .findAndAddModules()
                .build();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader reader = mapper.readerFor(BookingDtoRq.class);
        BookingDtoRq bookingDtoRq = reader.readValue(welcomeToTheClubBaddy);

        String expected = "SPQR";
        Mockito.when(bookingService.createBooking(bookingDtoRq)).thenThrow(new RuntimeException("Уже есть бронь"));

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(welcomeToTheClubBaddy)
                .when()
                .post("/booking")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(response.statusCode(), HttpStatus.BAD_REQUEST.value());
    }

    // тесты метода deleteBooking

    @Test
    //Сначало потестим успешное удаление брони
    public void deleteBooking() {
        String bookingNumber = "SPQR";

        Mockito.when(bookingService.deleteByNumber(bookingNumber)).thenReturn(true);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .pathParam("number", bookingNumber)
                .when()
                .delete("/booking/{number}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(response.statusCode(), HttpStatus.OK.value());
    }

    @Test
    //Теперь неуспешное удаление брони
    public void deleteBookingWithError() {
        String bookingNumber = "SPQR";

        Mockito.when(bookingService.deleteByNumber(bookingNumber)).thenReturn(false);

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .pathParam("number", bookingNumber)
                .when()
                .delete("/booking/{number}")
                .then()
                .extract()
                .response();

        Assertions.assertEquals(response.statusCode(), HttpStatus.BAD_REQUEST.value());
    }
}
