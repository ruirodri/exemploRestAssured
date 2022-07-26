package com.everis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static io.restassured.RestAssured.*;
import org.json.JSONObject;
import static org.hamcrest.Matchers.is;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(OrderAnnotation.class)
@Tag("booking")
public class BookingsAPITest {
    
    private static String url;
    private String path;
    private String token;
    private int newId;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd");
    

    /**
     * Inicialização da URL da API a ser acessada. Será feita uma vez, antes
     * da execução de todos os testes.
     */
    @BeforeAll
    public static void setupURI() {
        url = "https://restful-booker.herokuapp.com";
    }

     /**
     * Teste inicial, que obtém o token a ser utilizado posteriormente e verifica
     * se o status da resposta do token volta correto.
     */
    @Test
    @Order(1)
    public void testGettingToken() {
        path = "/auth";
        token = 
            given().
                header("Content-Type", "application/json").
                body(getTokenRequestBody()).
            when().
                post(url+path).
            then().
                assertThat().
                    statusCode(200).
                and().
                    extract().
                    response().
                    path("token");
    }

     /**
     * Teste que cria uma reserva baseada nos dados informados e verifica se o 
     * status da resposta volta correto.
     */
    @Test
    @Order(2)
    public void testCreateBooking() throws ParseException {
        path = "/booking";
        
        String requestData = createBookingRequestData("Rui", "Rodrigues", 1200, true, 
                    sdf.parse("2021-02-01"), sdf.parse("2021-02-02"), "Love"); 

        newId = given().
            header("Content-Type","application/json").
        when().
            body(requestData).
            post(url+path).
        then().
            assertThat().
                statusCode(200).
            and().
                extract().
                response().
                path("bookingid");
    }

    /**
     * Teste que verifica se a reserva que foi criada está consistente
     */
    @Test
    @Order(3)
    public void testBookingCreated() {
        path = "/booking/{id}";
        given().
            get(url+path, newId).
        then().
            assertThat().
                statusCode(is(200)).
                and().
                body("firstname", is("Rui")).
                and().
                body("additionalneeds", is("Love"));
    }

     /**
     * Testa a atualização da reserva que criamos anteriormente. 
     * @throws ParseException
     */
    @Test
    @Order(4)
    public void testUpdateBooking() throws ParseException{
        path = "/booking/"+String.valueOf(newId);
        
        String requestData = createBookingRequestData("Rui", "Rodrigues", 1500, true, 
                    sdf.parse("2021-02-01"), sdf.parse("2021-02-02"), "Love"); 

        given().
            header("Content-Type","application/json").
            header("Accept","application/json").
            header("Cookie","token="+token).
        when().
            body(requestData).
            put(url+path).
        then().
            assertThat().
                statusCode(200).
            and().
                body("totalprice", is(1500));
    }

     /**
     * Testa a atualização parcial da reserva que criamos anteriormente. 
     * @throws ParseException
     */
    @Test
    @Order(5)
    public void testPartialUpdateBooking() {
        path = "/booking/{id}";
        String request = "{\"lastname\":\"GuessWhat\"}";

        given().
            header("Content-Type","application/json").
            header("Accept","application/json").
            header("Cookie","token="+token).
        when().
            body(request).
            patch(url+path, newId).
        then().
            assertThat().
                statusCode(200).
            and().
                body("lastname", is("GuessWhat"));
    }

    /**
     * Testa se os valores que foram atualizados foram de fato salvos na base de dados
     */
    @Test
    @Order(6)
    public void testUpdatedValues() {
        path = "/booking/{id}";
        given().
            get(url+path, newId).
        then().
            assertThat().
                statusCode(is(200)).
                and().
                body("totalprice", is(1500)).
                and().
                body("lastname", is("GuessWhat"));
    }

    /**
     * Testa a exclusão da reserva criada
     */
    @Test
    @Order(7)
    public void testDeleteBooking() {
        path = "/booking/{id}";
        given().
            header("Content-Type","application/json").
            header("Cookie","token="+token).
            delete(url+path, newId).
        then().
            assertThat().
                statusCode(is(201));
    }

    /**
     * Testa se a reserva que foi excluída de fato não está mais na base de dados
     */
    @Test
    @Order(8)
    public void testSucessfullyDeleted() {
        path = "/booking/{id}";
        given().
            get(url+path, newId).
        then().
            assertThat().
                statusCode(is(404));
    }


    /**
     * Método que retorna o body a enviar no request para obter o token
     * @return a String com o JSON de request
     */
    private String getTokenRequestBody() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("username", "admin");
        requestParams.put("password", "password123");

        String retorno = requestParams.toString();

        return retorno;
    }

    /**
     * Método que cria um request para a criação de um novo agendamento
     * @param firstName - nome do cliente
     * @param lastName - sobrenome do cliente
     * @param totalPrice - preço total, sem centavos (inteiro)
     * @param depositPaid - boolean indicando se o deposito foi ou não efetuado
     * @param checkinDate - Data de check in
     * @param checkoutDate - Data de check out
     * @param additionalNeeds - Descrição de necessidades adicionais
     * @return uma String com os dados em formato JSON
     */
    private String createBookingRequestData(String firstName, String lastName, 
            int totalPrice, boolean depositPaid, Date checkinDate, Date checkoutDate,
            String additionalNeeds) {
        JSONObject bookingDates = new JSONObject();
        bookingDates.put("checkin", sdf.format(checkinDate));
        bookingDates.put("checkout", sdf.format(checkoutDate));

        JSONObject request = new JSONObject();
        request.put("firstname", firstName);
        request.put("lastname", lastName);
        request.put("totalprice", totalPrice);
        request.put("depositpaid", depositPaid);
        request.put("bookingdates", bookingDates);
        request.put("additionalneeds", additionalNeeds);

        return request.toString();
    }

}
