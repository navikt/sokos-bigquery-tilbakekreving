import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.sokos.bigquery.tilbakekreving.database.RepositoryExtensions.toTilbakekrevingOSObject
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TBKRAV
import no.nav.sokos.bigquery.tilbakekreving.domain.os.TilbakekrevingOSObject
import java.sql.ResultSet

internal class OSTilbakekrevingMappingTest : FunSpec({
    lateinit var resultSet: ResultSet
    beforeEach {
        resultSet = mockk<ResultSet>().apply {
            every { next() } returns true andThen false
            every { getInt(TBKRAV.feilUtbetalingID) } returns tKrav.feilUtbetalingID!!
            every { getInt(TBKRAV.lopenr) } returns tKrav.lopenr!!
            every { getString(TBKRAV.kodeStatusVedtak) } returns tKrav.kodeStatusVedtak!!
            every { getString(TBKRAV.brukerID) } returns tKrav.brukerID!!
            every { getString(TBKRAV.tidspunktReg) } returns tKrav.tidspunktReg!!
            every { getString(TBKRAV.kode) } returns tKrav.kode!!
            every { getString(TBKRAV.melding) } returns tKrav.melding!!
        }
    }

    test("Skal mappe respons fra OS til en liste av TilbakeKreving") {
        with(resultSet.toTilbakekrevingOSObject().first()) {
            feilUtbetalingID.shouldBe(tKrav.feilUtbetalingID)
            lopenr.shouldBe(tKrav.lopenr)
            kodeStatusVedtak.shouldBe(tKrav.kodeStatusVedtak)
            brukerID.shouldBe(tKrav.brukerID)
            tidspunktReg.shouldBe(tKrav.tidspunktReg)
            kode.shouldBe(tKrav.kode)
            melding.shouldBe(tKrav.melding)
        }
    }
})

private val tKrav = TilbakekrevingOSObject(
    feilUtbetalingID = 1,
    lopenr = 9999,
    kodeStatusVedtak = "BEAU",
    brukerID = "MJAU123",
    tidspunktReg = "2023-05-05",
    kode = "kode",
    melding = "melding",
    ID=""
)