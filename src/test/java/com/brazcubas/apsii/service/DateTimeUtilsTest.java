package com.brazcubas.apsii.service;

import com.brazcubas.apsii.config.DateTimeUtils;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Testes do DateTimeUtils.parseUtc: três formatos aceitos (Instant com "Z",
 * OffsetDateTime e LocalDateTime sem zona, tratado como UTC), uma borda de
 * calendário e os caminhos tristes de entrada inválida.
 */
class DateTimeUtilsTest {

    // ---------- Três formatos aceitos ----------

    @Test
    void parsesInstantWithZSuffix() {
        // Arrange
        String input = "2026-10-15T20:41:00Z";

        // Act
        Instant result = DateTimeUtils.parseUtc(input);

        // Assert
        assertEquals(Instant.parse("2026-10-15T20:41:00Z"), result);
    }

    @Test
    void parsesOffsetDateTimeAndConvertsToUtc() {
        // -03:00 significa que o UTC está 3 horas à frente
        String input = "2026-10-15T20:41:00-03:00";

        Instant result = DateTimeUtils.parseUtc(input);

        assertEquals(Instant.parse("2026-10-15T23:41:00Z"), result);
    }

    @Test
    void parsesLocalDateTimeWithoutZoneAsUtc() {
        String input = "2026-10-15T20:41:00";

        Instant result = DateTimeUtils.parseUtc(input);

        assertEquals(Instant.parse("2026-10-15T20:41:00Z"), result);
    }

    // ---------- Borda de calendário ----------

    @Test
    void acceptsLeapDayInLeapYear() {
        String input = "2024-02-29T23:59:59Z";

        Instant result = DateTimeUtils.parseUtc(input);

        assertEquals(Instant.parse("2024-02-29T23:59:59Z"), result);
    }

    @Test
    void rejectsLeapDayInNonLeapYear() {
        String input = "2026-02-29T00:00:00Z";

        assertThrows(DateTimeParseException.class, () -> DateTimeUtils.parseUtc(input));
    }

    // ---------- Caminho triste: entrada inválida ----------

    @Test
    void rejectsGarbageText() {
        assertThrows(DateTimeParseException.class, () -> DateTimeUtils.parseUtc("not-a-date"));
    }

    @Test
    void rejectsEmptyString() {
        assertThrows(DateTimeParseException.class, () -> DateTimeUtils.parseUtc(""));
    }

    @Test
    void rejectsDateWithoutTimeComponent() {
        assertThrows(DateTimeParseException.class, () -> DateTimeUtils.parseUtc("2026-10-15"));
    }

    @Test
    void nullInputThrowsNullPointerExceptionInsteadOfParseException() {
        // Documenta o comportamento ATUAL: o método só captura DateTimeParseException,
        // então null escapa como NullPointerException. Se a equipe decidir que null
        // deve virar um erro de validação, este teste deve ser ajustado junto com o código.
        assertThrows(NullPointerException.class, () -> DateTimeUtils.parseUtc(null));
    }
}
