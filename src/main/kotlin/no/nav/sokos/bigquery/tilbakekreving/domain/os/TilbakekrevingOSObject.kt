package no.nav.sokos.bigquery.tilbakekreving.domain.os


data class TilbakekrevingOSObject(
    val feilUtbetalingID: Int?,
    val lopenr: Int?,
    val kodeStatusVedtak: String?,
    val brukerID: String?,
    val tidspunktReg: String?,
    val kode: String?,
    val melding: String?,
    val ID: String,
)