package no.nav.sokos.bigquery.tilbakekreving.domain.bigquery

import java.time.LocalDate

data class TilbakekrevingBQTable(
    val ID: String = "ID",
    val datasetID: String = "Tilbakekreving",
    val tableID: String = "TEST_TILBAKEKREVING",
    val bqDatoHentet: String = "BQ_DATO_HENTET",
    val defBQSisteDatoHentet: LocalDate = LocalDate.of(2023, 1, 1),
    val feilUtbetalingID: String = "FEILUTB_ID",
    val lopenr: String = "LOPENR",
    val kodeStatusVedtak: String = "KODE_STATUS_VEDTAK",
    val brukerID: String = "BRUKERID",
    val tidspunktReg: String = "TIDSPKT_REG",
    val kode: String = "KODE",
    val melding: String = "MELDING",
)