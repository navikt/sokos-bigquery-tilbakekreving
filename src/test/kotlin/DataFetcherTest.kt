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

    val bqTilbakekrevingTabell = bigQueryTestUtils.createTilbakekrevingTabell()
    val datasetID = bqTilbakekrevingTabell.datasetID

    val bigQuery: BigQuery =
        bigQueryTestUtils.initBigQueryForTest(bigQueryTestUtils.createTilbakekrevingSchema(bqTilbakekrevingTabell))

    val db2DataSource = DatabaseTestUtils.getDataSource("init-tilbakekreving-db2.sql")

    val startDato = LocalDate.parse("2023-03-03")
    val sluttDato = LocalDate.parse("2023-03-05")

    afterEach {
        RemoteBigQueryHelper.forceDelete(bigQuery, datasetID)
    }

    test("Alle tilbakekrevingene for dato skal insertes i bigquery") {
        DataFetcher(db2DataSource, bqTilbakekrevingTabell).fetch(startDato, sluttDato)

        val query =
            QueryJobConfiguration
                .newBuilder("SELECT * FROM ${datasetID}.${tableName} ORDER BY ${bqTilbakekrevingTabell.feilUtbetalingID} ASC")
                .build()

        val queryResponse: TableResult = bigQuery.query(query)
        queryResponse.totalRows.shouldBe(8L)

        queryResponse.values.first().get(bqTilbakekrevingTabell.feilUtbetalingID).value.toString().shouldBe("8")
        queryResponse.values.last().get(bqTilbakekrevingTabell.feilUtbetalingID).value.toString().shouldBe("15")

        val rows = queryResponse.values.iterator()
        (8..15).forEach {
            with(rows.next()) {
                get(bqTilbakekrevingTabell.feilUtbetalingID).value.shouldBe("$it")
                get(bqTilbakekrevingTabell.lopenr).value.shouldBe("9999")
                get(bqTilbakekrevingTabell.kodeStatusVedtak).value.shouldBe("BEAU")
                get(bqTilbakekrevingTabell.brukerID).value.shouldBe("BRUKERID$it")
                val expectedDate = if (it < 13) startDato else if (it in 13..14) startDato.plusDays(1) else sluttDato
                get(bqTilbakekrevingTabell.tidspunktReg).value.toString().shouldStartWith(expectedDate.toString())
                get(bqTilbakekrevingTabell.kode).value.shouldBe("KODE$it")
                get(bqTilbakekrevingTabell.melding).value.shouldBe("MELDING$it")
            }
        }

    }
})