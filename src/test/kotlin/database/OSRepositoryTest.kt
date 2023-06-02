package database

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import no.nav.sokos.bigquery.tilbakekreving.database.OSRepository.getTilbakekrevingerForDate
import util.DatabaseTestUtils

internal class OSRepositoryTest : FunSpec({
    val datasource = DatabaseTestUtils.getDataSource("init-tilbakekreving-db2.sql")

    val dato1 = "2021-01-01"
    val dato2 = "2022-02-02"
    test("Skal hente riktig antall rader for tilbakekreving for dato") {
        datasource.connection.getTilbakekrevingerForDate(dato1).size.shouldBe(4)
        datasource.connection.getTilbakekrevingerForDate(dato2).size.shouldBe(3)
    }


    test("Skal hente data fra venteregister for dato") {
        (datasource.connection.getTilbakekrevingerForDate(dato1) + datasource.connection.getTilbakekrevingerForDate(dato2))
            .forEachIndexed { index, obj ->
                val nr = index + 1
                obj.feilUtbetalingID.shouldBe(nr)
                obj.brukerID.shouldBe("BRUKERID$nr")
                obj.kode.shouldBe("KODE$nr")
                obj.melding.shouldBe("MELDING$nr")
                obj.tidspunktReg.shouldStartWith(if (nr < 5) dato1 else dato2)
            }
    }
})