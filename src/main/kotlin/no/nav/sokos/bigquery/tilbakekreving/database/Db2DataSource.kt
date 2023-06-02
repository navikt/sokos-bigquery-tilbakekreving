package no.nav.sokos.bigquery.tilbakekreving.database


import com.ibm.db2.jcc.DB2BaseDataSource
import com.ibm.db2.jcc.DB2SimpleDataSource
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.sokos.bigquery.tilbakekreving.config.PropertiesConfig
import java.sql.Connection

class Db2DataSource(
    private val dbConfig: PropertiesConfig.DatabaseConfig = PropertiesConfig.Configuration().databaseConfig,
) {
    private val dataSource: HikariDataSource = HikariDataSource(hikariConfig())

    val connection: Connection get() = dataSource.connection
    fun close() = dataSource.close()
    private fun hikariConfig() = HikariConfig().apply {
        maximumPoolSize = 1000
        isAutoCommit = true
        poolName = "HikariPool-DB2"
        connectionTestQuery = "SELECT * FROM ${dbConfig.schema}.${dbConfig.testTable} FETCH FIRST 1 ROW ONLY;"
        dataSource = DB2SimpleDataSource().apply {
            driverType = 4
            enableNamedParameterMarkers = DB2BaseDataSource.YES
            databaseName = dbConfig.name
            serverName = dbConfig.host
            portNumber = dbConfig.port
            currentSchema = dbConfig.schema
            currentFunctionPath = dbConfig.schema
            connectionTimeout = 1000
            commandTimeout = 10000

            user = dbConfig.username.trim()
            setPassword(dbConfig.password.trim())
        }
    }
}