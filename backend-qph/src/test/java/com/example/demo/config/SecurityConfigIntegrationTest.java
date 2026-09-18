package com.example.demo.config;

import com.example.demo.security.JwtAuthFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.context.annotation.Import;

import org.springframework.http.MediaType;

import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import static org.springframework.security.test.web.servlet.request
        .SecurityMockMvcRequestPostProcessors.user;

import static org.springframework.test.web.servlet.request
        .MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result
        .MockMvcResultMatchers.*;


@WebMvcTest(
        controllers =
                SecurityTestController.class
)
@Import(SecurityConfig.class)
class SecurityConfigIntegrationTest {


    @Autowired
    private MockMvc mockMvc;


    /*
     * JwtAuthFilter ya tiene sus propias
     * pruebas unitarias.
     *
     * En este test queremos comprobar
     * SecurityConfig y sus reglas HTTP.
     */
    @MockBean
    private JwtAuthFilter jwtAuthFilter;


    /*
     * Dependencia requerida por
     * SecurityConfig.
     */
    @MockBean
    private UserDetailsService userDetailsService;


    @BeforeEach
    void configureJwtFilter()
            throws Exception {

        /*
         * Hacemos que el filtro JWT
         * simplemente deje continuar
         * la cadena de filtros.
         */
        doAnswer(invocation -> {

            ServletRequest request =
                    invocation.getArgument(0);

            ServletResponse response =
                    invocation.getArgument(1);

            FilterChain filterChain =
                    invocation.getArgument(2);


            filterChain.doFilter(
                    request,
                    response
            );

            return null;

        }).when(jwtAuthFilter)
                .doFilter(
                        any(ServletRequest.class),
                        any(ServletResponse.class),
                        any(FilterChain.class)
                );
    }


    // =========================================================
    // AUTH
    // =========================================================

    @Test
    void authEndpointShouldBePublic()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/auth/ping"
                        )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string("OK")
                );
    }


    // =========================================================
    // USERS
    // =========================================================

    @Test
    void adminShouldAccessUsers()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/users/test"
                        )
                                .with(
                                        user("admin")
                                                .roles("ADMIN")
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string("OK")
                );
    }


    @Test
    void normalUserShouldNotAccessUsers()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/users/test"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    @Test
    void anonymousUserShouldNotAccessUsers()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/users/test"
                        )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // =========================================================
    // CREAR VENTA
    // =========================================================

    @Test
    void userShouldCreateSale()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/sales"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                            "productId": 1,
                                            "quantity": 1
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string("OK")
                );
    }


    @Test
    void adminShouldCreateSale()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/sales"
                        )
                                .with(
                                        user("admin")
                                                .roles("ADMIN")
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                            "productId": 1,
                                            "quantity": 1
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    @Test
    void anonymousUserShouldNotCreateSale()
            throws Exception {

        mockMvc.perform(
                        post(
                                "/api/sales"
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                            "productId": 1,
                                            "quantity": 1
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // =========================================================
    // MIS VENTAS
    // =========================================================

    @Test
    void userShouldAccessOwnSales()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/sales/me"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    @Test
    void adminShouldAccessOwnSales()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/sales/me"
                        )
                                .with(
                                        user("admin")
                                                .roles("ADMIN")
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    // =========================================================
    // VENTA POR ID
    // =========================================================

    @Test
    void userShouldAccessSaleById()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/sales/123"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    @Test
    void adminShouldAccessSaleById()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/sales/123"
                        )
                                .with(
                                        user("admin")
                                                .roles("ADMIN")
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    // =========================================================
    // TODAS LAS VENTAS
    // =========================================================

    @Test
    void adminShouldAccessAllSales()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/sales"
                        )
                                .with(
                                        user("admin")
                                                .roles("ADMIN")
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    @Test
    void normalUserShouldNotAccessAllSales()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/sales"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // =========================================================
    // ENDPOINT PROTEGIDO GENÉRICO
    // =========================================================

    @Test
    void authenticatedUserShouldAccessProtectedEndpoint()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/products/test"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        content().string("OK")
                );
    }


    @Test
    void anonymousUserShouldNotAccessProtectedEndpoint()
            throws Exception {

        mockMvc.perform(
                        get(
                                "/api/products/test"
                        )
                )
                .andExpect(
                        status().isForbidden()
                );
    }


    // =========================================================
    // CSRF
    // =========================================================

    @Test
    void postShouldWorkWithoutCsrfToken()
            throws Exception {

        /*
         * SecurityConfig deshabilita CSRF.
         *
         * No utilizamos .with(csrf()).
         */
        mockMvc.perform(
                        post(
                                "/api/sales"
                        )
                                .with(
                                        user("angelo")
                                                .roles("USER")
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                            "productId": 1,
                                            "quantity": 1
                                        }
                                        """
                                )
                )
                .andExpect(
                        status().isOk()
                );
    }


    // =========================================================
    // CORS
    // =========================================================

    @Test
    void shouldAllowAngularOrigin()
            throws Exception {

        mockMvc.perform(
                        options(
                                "/api/products/test"
                        )
                                .header(
                                        "Origin",
                                        "http://localhost:4200"
                                )
                                .header(
                                        "Access-Control-Request-Method",
                                        "GET"
                                )
                )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        header().string(
                                "Access-Control-Allow-Origin",
                                "http://localhost:4200"
                        )
                );
    }
}