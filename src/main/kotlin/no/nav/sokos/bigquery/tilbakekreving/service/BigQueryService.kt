package no.nav.sokos.bigquery.tilbakekreving.service

import com.google.cloud.bigquery.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import no.nav.sokos.bigquery.tilbakekreving.config.PropertiesConfig
import no.nav.sokos.bigquery.tilbakekreving.domain.bigquery.TilbakekrevingBQTable
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSObject
import java.time.LocalDate
import kotlin.math.ceil
import kotlin.math.roundToInt


class BigQueryService(private val bigQuery: BigQuery = PropertiesConfig.Configuration().bigQueryConfig.bigQuery,) {
    private val log = KotlinLogging.logger {}
    private val maxNumberOfRowsToInsert = 10_000

    fun insert(tilbakekrevinger: List<TilbakekrevingOSObject>, table: TilbakekrevingBQTable) {
        val request = InsertAllRequest.newBuilder(table.datasetID, table.tableID)
        val sublists: List<List<TilbakekrevingOSObject>> = makeSublists(tilbakekrevinger)

        insertLists(sublists, request)
    }
    private fun insertLists(
        sublists: List<List<TilbakekrevingOSObject>>,
        request: InsertAllRequest.Builder,
    ) =
        runBlocking {
            sublists.map { sublist ->
                async(Dispatchers.IO) {
                    try {
                        val insertAllRequest = request.setRows(buildRows(sublist)).build()
                        val response: InsertAllResponse = bigQuery.insertAll(insertAllRequest)
                        var errorString = ""

                        if (response.hasErrors()) {
                            response.insertErrors.entries.forEach { entry ->
                                errorString += "error in entry ${entry.key}: ${entry.value}/n"
                            }
                            log.error(errorString)
                        }
                    } catch (e: BigQueryException) {
                        log.error(e.stackTraceToString())
                    }
                }
            }.awaitAll()
            return@runBlocking
        }

    fun getLastFetchedDate(table: TilbakekrevingBQTable): String {
        val query = "SELECT max(${table.bqDatoHentet}) FROM `${table.datasetID}.${table.tableID}`"
        val queryConfig: QueryJobConfiguration = QueryJobConfiguration.of(query)
        return try {
            bigQuery.query(queryConfig).values.first().first().stringValue
        } catch (e: Exception) {
            log.error("Ingen data hentet tidligere. Bruker default verdi: ${table.defBQSisteDatoHentet}")
            table.defBQSisteDatoHentet.toString()
        }
    }

    private fun makeSublists(objectsToInsert: List<TilbakekrevingOSObject>): List<List<TilbakekrevingOSObject>> {
        val size = objectsToInsert.size
        val numberOfJobs = ceil(size.toDouble() / maxNumberOfRowsToInsert.toDouble()).roundToInt()

        return (0 until numberOfJobs).map {
            val subListStart: Int = maxNumberOfRowsToInsert * it
            var subListEnd: Int = maxNumberOfRowsToInsert * (it + 1)
            if (subListEnd > size) subListEnd = size

            objectsToInsert.subList(subListStart, subListEnd)
        }
    }

    private fun buildRows(list: List<TilbakekrevingOSObject>): List<InsertAllRequest.RowToInsert> =
        list.map { InsertAllRequest.RowToInsert.of(mapOSobjectsToBqTable(it)) }
    private fun mapOSobjectsToBqTable(osObj: TilbakekrevingOSObject): Map<String?, Any?> {
        val table = TilbakekrevingBQTable()
        return mapOf(
            table.ID to "${osObj.feilUtbetalingID}--${osObj.tidspunktReg}",
            table.feilUtbetalingID to osObj.feilUtbetalingID,
            table.lopenr to osObj.lopenr,
            table.kodeStatusVedtak to osObj.kodeStatusVedtak,
            table.brukerID to osObj.brukerID,
            table.tidspunktReg to osObj.tidspunktReg,
            table.kode to osObj.kode,
            table.melding to osObj.melding,
            table.bqDatoHentet to LocalDate.now().toString()
        )
    }
}