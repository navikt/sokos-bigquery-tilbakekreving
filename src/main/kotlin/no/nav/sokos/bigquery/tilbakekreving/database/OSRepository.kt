package no.nav.sokos.bigquery.tilbakekreving.database

import mu.KotlinLogging
import no.nav.sokos.bigquery.tilbakekreving.database.RepositoryExtensions.toTilbakekrevingOSObject
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TBKRAV
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSObject
import java.sql.Connection

object OSRepository {
    private val log = KotlinLogging.logger {}

    fun Connection.setAcceleration() {
        try {
            prepareStatement("SET CURRENT QUERY ACCELERATION ALL;").execute()
        } catch (e: Exception) {
            log.error("Exception i acceleration: ${e.message}")
        }
    }

    fun Connection.getTilbakekrevingerForDate(date: String): List<TilbakekrevingOSObject> {
        return try {
            prepareStatement(
                """
              SELECT  ${TBKRAV.feilUtbetalingID},
                      ${TBKRAV.lopenr},
                      ${TBKRAV.kodeStatusVedtak},
                      ${TBKRAV.brukerID},
                      ${TBKRAV.tidspunktReg},
                      ${TBKRAV.kode},
                      ${TBKRAV.melding}
        
              FROM    $schema.${TBKRAV.name} 
              WHERE   DATE(${TBKRAV.tidspunktReg}) = '$date'
              AND     ${TBKRAV.lopenr} = ${TBKRAV.defLopeNr}
              WITH UR
            """.trimIndent()
            ).executeQuery().toTilbakekrevingOSObject()
        } catch (e: Exception) {
            log.error("exception i henting av tilbakekrevingskrav: ${e.message}")
            listOf()
        } finally {
            close()
        }
    }
}