package com.brazcubas.apsii.repository;

import com.brazcubas.apsii.model.Forecast;
import com.brazcubas.apsii.model.PredictionModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Testa, pela via pública (load, findAll e findByModelAndProbe), como o
 * CsvForecastRepository interpreta o campo opcional model_confidence:
 * ausente ou em branco vira null; número finito entre 0 e 1 é aceito
 * (bordas inclusivas); texto ou valor fora da faixa descarta a linha.
 *
 * Fixture: src/test/resources/data-test/predictions-11-2.csv
 *   mock-a-1  0.82   aceita
 *   mock-a-2  (vazio) aceita com null
 *   mock-a-3  0      aceita (limite inferior)
 *   mock-a-4  1      aceita (limite superior)
 *   mock-a-5  1.5    descartada (acima da faixa)
 *   mock-a-6  abc    descartada (não numérico)
 *   mock-a-7  -0.1   descartada (abaixo da faixa)
 *   mock-b-1  0.92   model-b, outra probe (900002)
 *   mock-zz-1 0.5    model-zz não registrado: ignorada por outra regra
 */
@ExtendWith(MockitoExtension.class)
class CsvForecastRepositoryConfidenceTest {
    private static final String FIXTURE = "classpath:data-test/predictions-11-2.csv";
    private static final int PROBE_ID = 900001;

    @Mock
    private ModelRepository repository;

    // ---------- Aceitas ----------

    @Test
    void confidenceInsideRangeIsAcceptedWithItsValue() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();
        Forecast row = timelineRow(forecasts, "mock-a-1");

        assertNotNull(row, "confiança 0.82 está dentro da faixa e deve ser aceita");
        assertEquals(Double.valueOf(0.82), row.modelConfidence());
    }

    @Test
    void blankConfidenceLoadsRowWithNullModelConfidence() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();
        Forecast row = timelineRow(forecasts, "mock-a-2");

        assertNotNull(row, "linha com confiança em branco deve ser carregada");
        assertNull(row.modelConfidence());
    }

    @Test
    void confidenceZeroIsAcceptedAtLowerBound() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();
        Forecast row = timelineRow(forecasts, "mock-a-3");

        assertNotNull(row, "confiança 0 é limite inclusivo e deve ser aceita");
        assertEquals(Double.valueOf(0.0), row.modelConfidence());
    }

    @Test
    void confidenceOneIsAcceptedAtUpperBound() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();
        Forecast row = timelineRow(forecasts, "mock-a-4");

        assertNotNull(row, "confiança 1 é limite inclusivo e deve ser aceita");
        assertEquals(Double.valueOf(1.0), row.modelConfidence());
    }

    // ---------- Descartadas ----------

    @Test
    void confidenceAboveOneDiscardsRow() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();

        assertTrue(isDiscarded(forecasts, "mock-a-5"), "1.5 está acima da faixa 0..1");
    }

    @Test
    void nonNumericConfidenceDiscardsRow() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();

        assertTrue(isDiscarded(forecasts, "mock-a-6"), "\"abc\" não é número");
    }

    @Test
    void negativeConfidenceDiscardsRow() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();

        assertTrue(isDiscarded(forecasts, "mock-a-7"), "-0.1 está abaixo da faixa 0..1");
    }

    // ---------- Resultado final da carga ----------

    @Test
    void timelineKeepsOnlyValidRowsInChronologicalOrder() {
        CsvForecastRepository forecasts = repositoryWithFixture();

        forecasts.load();
        List<Forecast> timeline = forecasts.findByModelAndProbe("model-a", PROBE_ID);

        assertEquals(List.of("mock-a-1", "mock-a-2", "mock-a-3", "mock-a-4"),
                timeline.stream().map(Forecast::predictionId).toList());
        // mock-zz-1 não aparece porque model-zz não está registrado (regra de outro campo)
        assertEquals(List.of("mock-a-1", "mock-a-2", "mock-a-3", "mock-a-4", "mock-b-1"),
                forecasts.findAll().stream().map(Forecast::predictionId).toList());
    }

    // ---------- Apoio ----------

    private CsvForecastRepository repositoryWithFixture() {
        when(repository.findAll()).thenReturn(List.of(
                model("model-a", "Modelo A"),
                model("model-b", "Modelo B")));
        return new CsvForecastRepository(new DefaultResourceLoader(), repository, FIXTURE);
    }

    private static Forecast timelineRow(CsvForecastRepository forecasts, String predictionId) {
        return forecasts.findByModelAndProbe("model-a", PROBE_ID).stream()
                .filter(forecast -> forecast.predictionId().equals(predictionId))
                .findFirst()
                .orElse(null);
    }

    private static boolean isDiscarded(CsvForecastRepository forecasts, String predictionId) {
        boolean inTimeline = forecasts.findByModelAndProbe("model-a", PROBE_ID).stream()
                .anyMatch(forecast -> forecast.predictionId().equals(predictionId));
        boolean inAll = forecasts.findAll().stream()
                .anyMatch(forecast -> forecast.predictionId().equals(predictionId));
        return !inTimeline && !inAll;
    }

    private static PredictionModel model(String id, String name) {
        return new PredictionModel(id, name, "Descrição de exemplo", "Grupo", "Teste", "1.0", true, null);
    }
}
