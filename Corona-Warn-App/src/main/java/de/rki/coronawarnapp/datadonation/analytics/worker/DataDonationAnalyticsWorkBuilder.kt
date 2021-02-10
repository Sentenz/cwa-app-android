package de.rki.coronawarnapp.datadonation.analytics.worker

import androidx.work.BackoffPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import dagger.Reusable
import de.rki.coronawarnapp.worker.BackgroundConstants
import org.joda.time.DateTimeConstants
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@Reusable
class DataDonationAnalyticsWorkBuilder @Inject constructor() {
    fun buildPeriodicWork(additionalDelay: Long): PeriodicWorkRequest =
        PeriodicWorkRequestBuilder<DataDonationAnalyticsPeriodicWorker>(
            DateTimeConstants.HOURS_PER_DAY.toLong(), TimeUnit.HOURS
        )
            .setInitialDelay(
                DateTimeConstants.HOURS_PER_DAY.toLong() + additionalDelay,
                TimeUnit.HOURS
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                BackgroundConstants.BACKOFF_INITIAL_DELAY,
                TimeUnit.MINUTES
            )
            .build()
}
