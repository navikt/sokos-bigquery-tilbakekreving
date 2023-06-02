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
    private val lastTimeFetched: LocalDate = LocalDate.parse(bigQueryService.getLastFetchedDate(table))

    fun fetch(from: LocalDate = lastTimeFetched, to: LocalDate = LocalDate.now()){
        var dateToFetch = from
        val totalRows = 0

        while (dateToFetch <= to) {
            val tilbakekrevinger = getTilbakekrevinger(dateToFetch.toString())
            bigQueryService.insert(tilbakekrevinger, table)
            totalRows.plus(tilbakekrevinger.size)
            dateToFetch = dateToFetch.plusDays(1)
        }
        log.info("Data fetch fullført med $totalRows rader kl  ${Calendar.getInstance().time}")

        dataSource.close()
    }
    private fun getTilbakekrevinger(dato: String): List<TilbakekrevingOSObject> = dataSource.connection.use { con ->
        con.setAcceleration()
        return con.getTilbakekrevingerForDate(dato)
    }

}