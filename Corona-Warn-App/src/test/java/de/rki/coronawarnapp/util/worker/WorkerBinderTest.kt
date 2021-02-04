package de.rki.coronawarnapp.util.worker

import android.content.Context
import androidx.work.ListenableWorker
import com.google.gson.Gson
import dagger.Component
import dagger.Module
import dagger.Provides
import de.rki.coronawarnapp.appconfig.AnalyticsConfig
import de.rki.coronawarnapp.appconfig.CWAConfig
import de.rki.coronawarnapp.appconfig.ExposureDetectionConfig
import de.rki.coronawarnapp.appconfig.ExposureWindowRiskCalculationConfig
import de.rki.coronawarnapp.appconfig.KeyDownloadConfig
import de.rki.coronawarnapp.appconfig.SurveyConfig
import de.rki.coronawarnapp.appconfig.download.AppConfigApiV2
import de.rki.coronawarnapp.datadonation.analytics.modules.DonorModule
import de.rki.coronawarnapp.datadonation.analytics.server.DataDonationAnalyticsApiV1
import de.rki.coronawarnapp.datadonation.safetynet.DeviceAttestation
import de.rki.coronawarnapp.deadman.DeadmanNotificationScheduler
import de.rki.coronawarnapp.deadman.DeadmanNotificationSender
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.notification.NotificationHelper
import de.rki.coronawarnapp.notification.TestResultAvailableNotificationService
import de.rki.coronawarnapp.playbook.Playbook
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.task.TaskController
import de.rki.coronawarnapp.util.coroutine.AppScope
import de.rki.coronawarnapp.util.coroutine.DispatcherProvider
import de.rki.coronawarnapp.util.di.AppContext
import de.rki.coronawarnapp.util.serialization.BaseGson
import io.github.classgraph.ClassGraph
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import org.junit.jupiter.api.Test
import testhelpers.BaseTest
import timber.log.Timber
import javax.inject.Provider
import javax.inject.Singleton

class WorkerBinderTest : BaseTest() {

    /**
     * If one of our factories is not part of the factory map provided to **[CWAWorkerFactory]**,
     * then the lookup will fail and an exception thrown.
     * This can't be checked at compile-time and may create subtle errors that will not immediately be caught.
     *
     * This test uses the ClassGraph library to scan our package, find all worker classes,
     * and makes sure that they are all bound into our factory map.
     * Creating a new factory that is not registered or removing one from **[WorkerBinder]**
     * will cause this test to fail.
     */
    @Test
    fun `all worker factory are bound into the factory map`() {
        val component = DaggerWorkerTestComponent.factory().create()
        val factories = component.factories

        Timber.v("We know %d worker factories.", factories.size)
        factories.keys.forEach {
            Timber.v("Registered: ${it.name}")
        }
        require(component.factories.isNotEmpty())

        val scanResult = ClassGraph()
            .acceptPackages("de.rki.coronawarnapp")
            .enableClassInfo()
            .scan()

        val ourWorkerClasses = scanResult
            .getSubclasses("androidx.work.ListenableWorker")
            .filterNot { it.name.startsWith("androidx.work") }

        Timber.v("Our project contains %d worker classes.", ourWorkerClasses.size)
        ourWorkerClasses.forEach { Timber.v("Existing: ${it.name}") }

        val boundFactories = factories.keys.map { it.name }
        val existingFactories = ourWorkerClasses.map { it.name }
        boundFactories shouldContainAll existingFactories
    }
}

@Singleton
@Component(modules = [WorkerBinder::class, MockProvider::class])
interface WorkerTestComponent {

    val factories: @JvmSuppressWildcards Map<Class<out ListenableWorker>, Provider<InjectedWorkerFactory<out ListenableWorker>>>

    @Component.Factory
    interface Factory {
        fun create(): WorkerTestComponent
    }
}

@Module
class MockProvider {
    // For BackgroundNoiseOneTimeWorker
    @Provides
    fun playbook(): Playbook = mockk()

    // For DeadmanNotificationScheduler
    @Provides
    fun sender(): DeadmanNotificationSender = mockk()

    // For DeadmanNotificationPeriodicWorker
    @Provides
    fun scheduler(): DeadmanNotificationScheduler = mockk()

    @Provides
    fun taskController(): TaskController = mockk()

    // For ExposureStateUpdateWorker
    @Provides
    fun enfClient(): ENFClient = mockk()

    @Provides
    fun exposureSummaryRepository(): RiskLevelStorage = mockk()

    @Provides
    fun testResultAvailableNotification(): TestResultAvailableNotificationService = mockk()

    @Provides
    fun notificationHelper(): NotificationHelper = mockk()

    // For Analytics periodic worker
    @Provides
    fun deviceAttestation(): DeviceAttestation = mockk()

    @Provides
    fun donorModuleSet(): Set<DonorModule> = mockk()

    @Provides
    fun dataDonationAnalyticsApiV1(): DataDonationAnalyticsApiV1 = mockk()

    @Provides
    fun dispatcherProvider(): DispatcherProvider = mockk()

    @Provides
    @AppScope
    fun coroutineScope(): CoroutineScope = mockk()

    @Provides
    fun appConfigApiV2(): AppConfigApiV2 = mockk()

    @Provides
    fun cwaConfigMapper(): CWAConfig.Mapper = mockk()

    @Provides
    fun keyDownloadConfigMapper(): KeyDownloadConfig.Mapper = mockk()

    @Provides
    fun exposureDetectionConfigMapper(): ExposureDetectionConfig.Mapper = mockk()

    @Provides
    fun exposureWindowRiskCalculationConfigMapper(): ExposureWindowRiskCalculationConfig.Mapper = mockk()

    @Provides
    fun analyticsConfigMapper(): AnalyticsConfig.Mapper = mockk()

    @Provides
    fun surveyConfigMapper(): SurveyConfig.Mapper = mockk()

    @Provides
    @AppContext
    fun context(): Context = mockk()

    @Provides
    @BaseGson
    fun baseGson(): Gson = mockk()
}
