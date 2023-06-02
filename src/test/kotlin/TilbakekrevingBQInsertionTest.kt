import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.QueryJobConfiguration
import com.google.cloud.bigquery.TableResult
import com.google.cloud.bigquery.testing.RemoteBigQueryHelper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSObject
import no.nav.sokos.bigquery.tilbakekreving.service.BigQueryService
import util.BigQueryTestUtils
import java.time.LocalDate


private const val NUMBER_OF_R0WS_TO_INSERT = 100_000

internal class TilbakekrevingBQInsertionTest : FunSpec({

    val tableName = this::class.simpleName.toString()
    val bigQueryTestUtils = BigQueryTestUtils(tableName = tableName)
    val bqTilbakekrevingTabell = bigQueryTestUtils.createTilbakekrevingTabell()
    val datasetID = bqTilbakekrevingTabell.datasetID

    val bigQuery: BigQuery =
        bigQueryTestUtils.initBigQueryForTest(bigQueryTestUtils.createTilbakekrevingSchema(bqTilbakekrevingTabell))

    val bigQueryService = BigQueryService(bigQuery)


    afterEach {
        RemoteBigQueryHelper.forceDelete(bigQuery, datasetID)
    }

    test("Skal inserte alle rader i tabell") {
        val tKravList = lagTilbakeKreving()
        bigQueryService.insert(tKravList, bqTilbakekrevingTabell)

        val query =
            QueryJobConfiguration
                .newBuilder("SELECT * FROM ${datasetID}.${tableName} ORDER BY ${bqTilbakekrevingTabell.feilUtbetalingID} ASC")
                .build()

        val queryResponse: TableResult = bigQuery.query(query)
        queryResponse.totalRows.shouldBe(NUMBER_OF_R0WS_TO_INSERT)

        with(queryResponse.values.first()) {
            get(bqTilbakekrevingTabell.feilUtbetalingID).value.shouldBe(tKravList.first().feilUtbetalingID.toString())
            get(bqTilbakekrevingTabell.lopenr).value.shouldBe("9999")
            get(bqTilbakekrevingTabell.kodeStatusVedtak).value.shouldBe("BEAU")
            get(bqTilbakekrevingTabell.brukerID).value.shouldBe("MJAU123")
            get(bqTilbakekrevingTabell.tidspunktReg).value.shouldBe("2023-05-05")
            get(bqTilbakekrevingTabell.bqDatoHentet).value.shouldBe(LocalDate.now().toString())
        }
    }
})

private fun lagTilbakeKreving(): MutableList<TilbakekrevingOSObject> =
    mutableListOf<TilbakekrevingOSObject>().apply {
        (1..NUMBER_OF_R0WS_TO_INSERT).forEach {
            add(
                TilbakekrevingOSObject(
                    feilUtbetalingID = it,
                    lopenr = 9999,
                    kodeStatusVedtak = "BEAU",
                    brukerID = "MJAU123",
                    tidspunktReg = "2023-05-05",
                    kode = "",
                    melding = "",
                    ID=""
                )
            )
        }
    }
