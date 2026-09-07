package com.umanizales.pagos.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class StrongPasswordValidatorTest {

    private final StrongPasswordValidator validator = new StrongPasswordValidator();

    @ParameterizedTest
    @ValueSource(strings = {"short1A", "sinmayuscula1", "SinDigitos", ""})
    void rechazaContraseniasQueNoCumplenLaPolitica(String password) {
        assertThat(validator.isValid(password, null)).isFalse();
    }

    @Test
    void rechazaContraseniaNula() {
        assertThat(validator.isValid(null, null)).isFalse();
    }

    @Test
    void aceptaContraseniaConMayusculaDigitoYLongitudMinima() {
        assertThat(validator.isValid("Segura123", null)).isTrue();
    }
}
