package util

import com.google.cloud.bigquery.*
import com.google.cloud.bigquery.testing.RemoteBigQueryHelper
import no.nav.sokos.bigquery.tilbakekreving.config.PropertiesConfig
import no.nav.sokos.bigquery.tilbakekreving.domain.bigquery.TilbakekrevingBQTable

class BigQueryTestUtils(
    val datasetID: String = RemoteBigQueryHelper.generateDatasetName(),
    var tableName: String = "bq_test"
) {
    private val datasetInfo: DatasetInfo = DatasetInfo.newBuilder(datasetID).setLocation("europe-north1").build()

    fun initBigQueryForTest(schema: Schema): BigQuery {
        val tableDefinition: TableDefinition = StandardTableDefinition.of(schema)
        val tableInfo = TableInfo.newBuilder(TableId.of(datasetID, tableName), tableDefinition).build()

        return PropertiesConfig.BigQueryConfig().bigQuery.apply {
            if (getDataset(datasetID) == null) create(datasetInfo)
            create(tableInfo)
        }
    }

    fun createTilbakekrevingTabell() = TilbakekrevingBQTable(datasetID = datasetID, tableName = tableName)

    fun createTilbakekrevingSchema(bq: TilbakekrevingBQTable): Schema =
        Schema.of(
            Field.of(bq.feilUtbetalingID, StandardSQLTypeName.INT64),
            Field.of(bq.lopenr, StandardSQLTypeName.INT64),
            Field.of(bq.kodeStatusVedtak, StandardSQLTypeName.STRING),
            Field.of(bq.brukerID, StandardSQLTypeName.STRING),
            Field.of(bq.tidspunktReg, StandardSQLTypeName.STRING),
            Field.of(bq.kode, StandardSQLTypeName.STRING),
            Field.of(bq.melding, StandardSQLTypeName.STRING),
            Field.of(bq.bqDatoHentet, StandardSQLTypeName.DATE),
        )
}