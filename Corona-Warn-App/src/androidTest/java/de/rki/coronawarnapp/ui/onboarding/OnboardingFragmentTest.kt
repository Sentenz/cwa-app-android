package de.rki.coronawarnapp.ui.onboarding

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import androidx.fragment.app.testing.launchFragment
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import dagger.Module
import dagger.android.ContributesAndroidInjector
import de.rki.coronawarnapp.R
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import testhelpers.BaseUITest
import testhelpers.CWScreenshotCallback
import testhelpers.SCREENSHOT_DELAY_TIME
import testhelpers.Screenshot
import testhelpers.SystemUIDemoModeRule
import testhelpers.launchFragmentInContainer2
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.ScreenshotStrategy
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy
import tools.fastlane.screengrab.locale.LocaleTestRule
import java.util.jar.Manifest

@RunWith(AndroidJUnit4::class)
class OnboardingFragmentTest : BaseUITest() {

//    @Rule
//    @JvmField
//    val localeTestRule = LocaleTestRule()
//
    @get:Rule
    val systemUIDemoModeRule = SystemUIDemoModeRule()

    @get:Rule
    val grantPermissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        WRITE_EXTERNAL_STORAGE,
        READ_EXTERNAL_STORAGE
    )

    @Before
    fun setUp() {
        setupMockViewModel(object : OnboardingFragmentViewModel.Factory {
            override fun create(): OnboardingFragmentViewModel = OnboardingFragmentViewModel()
        })
    }

    @After
    fun teardown() {
        clearAllViewModels()
    }

    @Test
    fun launch_fragment() {
        launchFragment<OnboardingFragment>()
    }

    @Screenshot
    @Test
    fun capture_screenshot() {
        launchFragmentInContainer2<OnboardingFragment>()
        Thread.sleep(SCREENSHOT_DELAY_TIME)
        Screengrab.screenshot(OnboardingFragment::class.simpleName)

        onView(withId(R.id.onboarding_easy_language)).perform(scrollTo(), click())
        Screengrab.screenshot(
            OnboardingFragment::class.simpleName.plus("2"),
            UiAutomatorScreenshotStrategy(),
            CWScreenshotCallback()
        )
    }
}

@Module
abstract class OnboardingFragmentTestModule {
    @ContributesAndroidInjector
    abstract fun onboardingFragment(): OnboardingFragment
}
