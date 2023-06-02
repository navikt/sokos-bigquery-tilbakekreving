package no.nav.sokos.bigquery.tilbakekreving.database


import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSObject
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSTable
import java.sql.ResultSet

object RepositoryExtensions {
    private val TBKRAV: TilbakekrevingOSTable = TilbakekrevingOSTable()
    fun ResultSet.toTilbakekrevingOSObject() =
        toList {
            TilbakekrevingOSObject(
                feilUtbetalingID = getInt(TBKRAV.feilUtbetalingID),
                lopenr = getInt(TBKRAV.lopenr),
                kodeStatusVedtak = getString(TBKRAV.kodeStatusVedtak),
                brukerID = getString(TBKRAV.brukerID),
                tidspunktReg = getString(TBKRAV.tidspunktReg),
                kode = getString(TBKRAV.kode),
                melding = getString(TBKRAV.melding),
                ID = "${getInt(TBKRAV.feilUtbetalingID)}-${getString(TBKRAV.tidspunktReg)}"
            )
        }

    private fun <T> ResultSet.toList(mapper: ResultSet.() -> T) = mutableListOf<T>().apply {
        while (next()) {
            add(mapper())
        }
    }
}