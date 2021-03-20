package de.rki.coronawarnapp.eventregistration.checkins

import de.rki.coronawarnapp.eventregistration.storage.TraceLocationDatabase
import de.rki.coronawarnapp.eventregistration.storage.dao.CheckInDao
import de.rki.coronawarnapp.eventregistration.storage.entity.TraceLocationCheckInEntity
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import okio.ByteString.Companion.EMPTY
import org.joda.time.Instant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import testhelpers.BaseTest

class CheckInRepositoryTest : BaseTest() {

    @MockK lateinit var factory: TraceLocationDatabase.Factory
    @MockK lateinit var database: TraceLocationDatabase
    @MockK lateinit var checkInDao: CheckInDao
    private val allEntriesFlow = MutableStateFlow(emptyList<TraceLocationCheckInEntity>())

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        every { factory.create() } returns database
        every { database.eventCheckInDao() } returns checkInDao
        every { checkInDao.allEntries() } returns allEntriesFlow
    }

    private fun createInstance(scope: CoroutineScope) = CheckInRepository(
        factory,
        scope
    )

    @Test
    fun `add new check in`() {
        coEvery { checkInDao.insert(any()) } returns 0L
        runBlockingTest {
            val time = Instant.ofEpochMilli(1397210400000)
            val end = Instant.ofEpochMilli(1397210400001)
            createInstance(scope = this).addCheckIn(
                CheckIn(
                    id = 0L,
                    guid = "41da2115-eba2-49bd-bf17-adb3d635ddaf",
                    guidHash = EMPTY,
                    version = 1,
                    type = 2,
                    description = "brothers birthday",
                    address = "Malibu",
                    traceLocationStart = time,
                    traceLocationEnd = null,
                    defaultCheckInLengthInMinutes = null,
                    traceLocationBytes = EMPTY,
                    signature = EMPTY,
                    checkInStart = time,
                    checkInEnd = end,
                    completed = false,
                    createJournalEntry = false
                )
            )
            coVerify {
                checkInDao.insert(
                    TraceLocationCheckInEntity(
                        id = 0L,
                        guid = "41da2115-eba2-49bd-bf17-adb3d635ddaf",
                        guidHashBase64 = "",
                        version = 1,
                        type = 2,
                        description = "brothers birthday",
                        address = "Malibu",
                        traceLocationStart = time,
                        traceLocationEnd = null,
                        defaultCheckInLengthInMinutes = null,
                        traceLocationBytesBase64 = "",
                        signatureBase64 = "",
                        checkInStart = time,
                        checkInEnd = end,
                        completed = false,
                        createJournalEntry = false
                    )
                )
            }
        }
    }

    @Test
    fun `update new check in`() {
        coEvery { checkInDao.update(any()) } returns Unit
        runBlockingTest {
            val start = Instant.ofEpochMilli(1397210400000)
            val end = Instant.ofEpochMilli(1615796487)
            createInstance(scope = this).updateCheckIn(
                CheckIn(
                    id = 0L,
                    guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                    guidHash = EMPTY,
                    version = 1,
                    type = 2,
                    description = "sisters birthday",
                    address = "Long Beach",
                    traceLocationStart = start,
                    traceLocationEnd = end,
                    defaultCheckInLengthInMinutes = null,
                    traceLocationBytes = EMPTY,
                    signature = EMPTY,
                    checkInStart = start,
                    checkInEnd = end,
                    completed = false,
                    createJournalEntry = false
                )
            )
            coVerify {
                checkInDao.update(
                    TraceLocationCheckInEntity(
                        id = 0L,
                        guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                        guidHashBase64 = "",
                        version = 1,
                        type = 2,
                        description = "sisters birthday",
                        address = "Long Beach",
                        traceLocationStart = start,
                        traceLocationEnd = end,
                        defaultCheckInLengthInMinutes = null,
                        traceLocationBytesBase64 = "",
                        signatureBase64 = "",
                        checkInStart = start,
                        checkInEnd = end,
                        completed = false,
                        createJournalEntry = false
                    )
                )
            }
        }
    }

    @Test
    fun `get data`() {
        val start = Instant.ofEpochMilli(1615796487)
        val end = Instant.ofEpochMilli(1397210400000)
        allEntriesFlow.value = listOf(
            TraceLocationCheckInEntity(
                id = 0L,
                guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                guidHashBase64 = "",
                version = 1,
                type = 2,
                description = "sisters birthday",
                address = "Long Beach",
                traceLocationStart = start,
                traceLocationEnd = end,
                defaultCheckInLengthInMinutes = null,
                traceLocationBytesBase64 = "",
                signatureBase64 = "",
                checkInStart = start,
                checkInEnd = end,
                completed = false,
                createJournalEntry = false
            )
        )
        runBlockingTest {
            createInstance(scope = this).allCheckIns.first() shouldBe listOf(
                CheckIn(
                    id = 0L,
                    guid = "6e5530ce-1afc-4695-a4fc-572e6443eacd",
                    guidHash = EMPTY,
                    traceLocationBytes = EMPTY,
                    version = 1,
                    type = 2,
                    description = "sisters birthday",
                    address = "Long Beach",
                    traceLocationStart = start,
                    traceLocationEnd = end,
                    defaultCheckInLengthInMinutes = null,
                    signature = EMPTY,
                    checkInStart = start,
                    checkInEnd = end,
                    completed = false,
                    createJournalEntry = false
                )
            )
        }
    }
}