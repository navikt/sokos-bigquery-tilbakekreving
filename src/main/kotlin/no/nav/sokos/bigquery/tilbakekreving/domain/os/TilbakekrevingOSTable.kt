package no.nav.sokos.bigquery.tilbakekreving.domain.os


data class TilbakekrevingOSTable(
    val name: String = "T7_VEDTAK_STATUS",
    val shortName: String = "STATUS",
    val feilUtbetalingID: String = "INTERN_FEILUTB_ID",
    val lopenr: String = "LOPENR",
    val kodeStatusVedtak: String = "KODE_STATUS_VEDTAK",
    val brukerID: String = "BRUKERID",
    val tidspunktReg: String = "TIDSPKT_REG",
    val kode: String = "KODE",
    val melding: String = "MELDING",
    val defLopeNr: Int = 9999
)