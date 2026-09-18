package com.example.demo;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class PruebaTecnicaApplicationTest {

    @Test
    void shouldStartSpringBootApplication() {

        String[] args = {};

        ConfigurableApplicationContext context =
                mock(
                        ConfigurableApplicationContext.class
                );

        try (
                MockedStatic<SpringApplication>
                        springApplication =
                        mockStatic(
                                SpringApplication.class
                        )
        ) {

            springApplication
                    .when(() ->
                            SpringApplication.run(
                                    PruebaTecnicaApplication.class,
                                    args
                            )
                    )
                    .thenReturn(context);


            PruebaTecnicaApplication.main(
                    args
            );


            springApplication.verify(() ->
                    SpringApplication.run(
                            PruebaTecnicaApplication.class,
                            args
                    )
            );
        }
    }
}