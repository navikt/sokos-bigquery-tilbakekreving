package integration

import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.QueryJobConfiguration
import com.google.cloud.bigquery.TableResult
import com.google.cloud.bigquery.testing.RemoteBigQueryHelper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import no.nav.sokos.bigquery.tilbakekreving.service.DataFetcher
import util.BigQueryTestUtils
import util.DatabaseTestUtils
import java.time.LocalDate

class DataFetcherTest: FunSpec ({

    val tableName = this::class.simpleName.toString()
    val bigQueryTestUtils = BigQueryTestUtils(tableName = tableName)

    val bqTilbakekrevingTable = bigQueryTestUtils.createTilbakekrevingTable()
    val datasetID = bqTilbakekrevingTable.datasetID

    val bigQuery: BigQuery =
        bigQueryTestUtils.initBigQueryForTest(bigQueryTestUtils.createTilbakekrevingSchema(bqTilbakekrevingTable))

    val db2DataSource = DatabaseTestUtils.getDataSource("init-tilbakekreving-db2.sql")

    val startDate = LocalDate.parse("2023-03-03")
    val endDate = LocalDate.parse("2023-03-05")

    afterEach {
        RemoteBigQueryHelper.forceDelete(bigQuery, datasetID)
    }

    test("Alle tilbakekrevingene for dato skal insertes i bigquery") {
        DataFetcher(db2DataSource, bqTilbakekrevingTable).fetch(startDate, endDate)

        val query = "SELECT * FROM ${datasetID}.${tableName} ORDER BY ${bqTilbakekrevingTable.feilUtbetalingID} ASC"
        val queryConfig = QueryJobConfiguration.of(query)

        val queryResponse: TableResult = bigQuery.query(queryConfig)
        queryResponse.totalRows.shouldBe(8L)

        queryResponse.values.first().get(bqTilbakekrevingTable.feilUtbetalingID).value.toString().shouldBe("8")
        queryResponse.values.last().get(bqTilbakekrevingTable.feilUtbetalingID).value.toString().shouldBe("15")

        val rows = queryResponse.values.iterator()
        (8..15).forEach {
            with(rows.next()) {
                get(bqTilbakekrevingTable.feilUtbetalingID).value.shouldBe("$it")
                get(bqTilbakekrevingTable.lopenr).value.shouldBe("9999")
                get(bqTilbakekrevingTable.kodeStatusVedtak).value.shouldBe("BEAU")
                get(bqTilbakekrevingTable.brukerID).value.shouldBe("BRUKERID$it")
                val expectedDate = if (it < 13) startDate else if (it in 13..14) startDate.plusDays(1) else endDate
                get(bqTilbakekrevingTable.tidspunktReg).value.toString().shouldStartWith(expectedDate.toString())
                get(bqTilbakekrevingTable.kode).value.shouldBe("KODE$it")
                get(bqTilbakekrevingTable.melding).value.shouldBe("MELDING$it")
            }
        }

    }
})