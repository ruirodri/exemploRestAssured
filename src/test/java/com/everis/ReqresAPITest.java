package com.everis;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.*;

import org.json.JSONObject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import static org.hamcrest.Matchers.is;

/**
 * Unit test for simple App.
 */
@Tag("reqres")
public class ReqresAPITest 
{
    private static final String url = "https://reqres.in/api";
    private String path;

    /**
     * Testa que, ao acessarmos a lista de usuários, o código de status retornado
     * é igual a 200.
     */
    @Test
    public void listaDeUsersRetornaCode200()
    {
        path="/users";

        Response response = RestAssured.get(url+path);
        assertEquals(200, response.statusCode());
    }

    /**
     * Testa que, ao acessarmos a lista de usuários, a quantidade total retornada é 12.
     */
    @Test
    public void quantidadeDeUsersIgualDoze()
    {
        path="/users";
        Response response = RestAssured.get(url+path);
        assertEquals(Integer.valueOf(12), response.path("total"));
    }

    /**
     * Testa o acesso a um usuário específico e verifica se o e-mail 
     * retornado está correto.
     */
    @Test
    public void verificaEmailDeUsuarioEspecifico()
    {
        path="/users/2";
        Response response = RestAssured.get(url+path);
        assertEquals("janet.weaver@reqres.in", response.path("data.email"));
    }


    /**
     * Testa que, quando acessamos um usuário que não existe, o código
     * de status retornado é 404.
     */
    @Test
    public void userInexistenteRetornaCode404()
    {
        path="/users/101";

        Response response = RestAssured.get(url+path);
        assertEquals(404, response.statusCode());
    }

    /**
     * Testa se o terceiro usuário retornado na segunda página
     * de resultados é o de ID 9.
     */
    @Test
    public void quandoObtenhoSegundaPaginaUsersTerceiroUserTemId9()
    {
        path = "/users";

        given().
            queryParam("page", 2).
        when().
            get(url+path).
        then().
            body("data[2]", hasEntry("id", 9) );
    }

    /**
     * Testa a criação de um usuário
     */
    @Test
    public void testaCriacaoUsuario()
    {
        path = "/users";

        JSONObject requestBody = new JSONObject();
        requestBody.put("name", "morpheus");
        requestBody.put("job", "leader");

        given().
            header("Content-Type","application/json").
            body(requestBody.toString()).
        when().
            post(url+path).
        then().
            body("name", is("morpheus") ).
            and().
            body("job", is("leader"));
    }


}
