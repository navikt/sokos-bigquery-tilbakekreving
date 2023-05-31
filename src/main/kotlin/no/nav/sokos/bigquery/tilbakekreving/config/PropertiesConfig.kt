package no.nav.sokos.bigquery.tilbakekreving.config

import com.google.api.gax.retrying.RetrySettings
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.cloud.bigquery.BigQuery
import com.google.cloud.bigquery.BigQueryOptions
import com.natpryce.konfig.*
import org.threeten.bp.Duration
import java.io.File


object PropertiesConfig {
    private val defaultProperties = ConfigurationMap(
        mapOf(
            "NAIS_APP_NAME" to "sokos-bigquery-tilbakekreving",
            "NAIS_NAMESPACE" to "okonomi",
        )
    )

    private val localDevProperties = ConfigurationMap(
        mapOf(
            "APPLICATION_PROFILE" to Profile.LOCAL.toString(),
            "USE_AUTHENTICATION" to "true",
            "DATABASE_HOST" to "host",
            "DATABASE_PORT" to "123",
            "DATABASE_NAME" to "name",
            "DATABASE_SCHEMA" to "schema",
            "OS_USERNAME" to "username",
            "OS_PASSWORD" to "password",
            "GOOGLE_APPLICATION_CREDENTIALS" to "googleApplicationCredentials"
        )
    )

    private val devProperties = ConfigurationMap(mapOf("APPLICATION_PROFILE" to Profile.DEV.toString()))
    private val prodProperties = ConfigurationMap(mapOf("APPLICATION_PROFILE" to Profile.PROD.toString()))
    private val config = when (System.getenv("NAIS_CLUSTER_NAME") ?: System.getProperty("NAIS_CLUSTER_NAME")) {
        "dev-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding devProperties overriding defaultProperties
        "prod-fss" -> ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding prodProperties overriding defaultProperties
        else -> {
            ConfigurationProperties.systemProperties() overriding EnvironmentVariables() overriding ConfigurationProperties.fromOptionalFile(
                File("defaults.properties")
            ) overriding localDevProperties overriding defaultProperties
        }
    }

    private operator fun get(key: String): String = config[Key(key, stringType)]

    data class Configuration(
        val naisAppName: String = get("NAIS_APP_NAME"),
        val profile: Profile = Profile.valueOf(this["APPLICATION_PROFILE"]),
        val databaseConfig: OppdragZConfig = OppdragZConfig(),
        val bigQueryConfig: BigQueryConfig = BigQueryConfig()
    )

    data class OppdragZConfig(
        val host: String = get("DATABASE_HOST"),
        val port: Int = get("DATABASE_PORT").toInt(),
        val name: String = get("DATABASE_NAME"),
        val schema: String = get("DATABASE_SCHEMA"),
        val username: String = get("OS_USERNAME"),
        val password: String = get("OS_PASSWORD"),
        val testTable: String = get("TEST_TABLE")
    )

    data class BigQueryConfig(
        private val sac: ServiceAccountCredentials? = ServiceAccountCredentials.fromStream(get("GOOGLE_APPLICATION_CREDENTIALS").byteInputStream()),
        private val retrySettings: RetrySettings? = RetrySettings.newBuilder().apply {
            maxRetryDelay = Duration.ofMillis(5000)
            maxAttempts = 10
        }.build(),
        val bigQuery: BigQuery = BigQueryOptions.newBuilder()
            .setCredentials(sac)
            .setRetrySettings(retrySettings)
            .build().service
    )

    enum class Profile {
        LOCAL, DEV, PROD
    }
}