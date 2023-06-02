package no.nav.sokos.bigquery.tilbakekreving.service

import mu.KLogger
import mu.KotlinLogging
import no.nav.sokos.bigquery.tilbakekreving.database.Db2DataSource
import no.nav.sokos.bigquery.tilbakekreving.database.OSRepository.getTilbakekrevingerForDate
import no.nav.sokos.bigquery.tilbakekreving.database.OSRepository.setAcceleration
import no.nav.sokos.bigquery.tilbakekreving.domain.bigquery.TilbakekrevingBQTable
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSObject
import java.time.LocalDate
import java.util.*

class DataFetcher(
    private val dataSource: Db2DataSource = Db2DataSource(),
    private val table: TilbakekrevingBQTable = TilbakekrevingBQTable(),
) {
    private val log: KLogger = KotlinLogging.logger {}
    private val bigQueryService: BigQueryService = BigQueryService()
    fun fetch(from: LocalDate = getLastTimeFetchedDate(), to: LocalDate = LocalDate.now()){
        var dateToFetch = from
        var totalrows = 0

        while (dateToFetch <= to) {
            val tilbakekrevinger = getTilbakekrevinger(dateToFetch.toString())

           bigQueryService.insert(tilbakekrevinger, table)
            totalrows += tilbakekrevinger.size
            dateToFetch = dateToFetch.plusDays(1)
        }
        log.info("Data fetch fullført med $totalrows rader kl  ${Calendar.getInstance().time}")

    }
    private fun getTilbakekrevinger(dato: String): List<TilbakekrevingOSObject> = dataSource.connection.use { con ->
        con.setAcceleration()
        return con.getTilbakekrevingerForDate(dato)
    }
    private fun getLastTimeFetchedDate(): LocalDate = LocalDate.parse(bigQueryService.getLastFetchedDate(table))
}