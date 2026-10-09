package com.bastyoliva.wearable.settings

import com.bastyoliva.wearable.data.repository.RemoteInteractionHandler
import com.bastyoliva.wearable.navigation.NavigationAction
import com.bastyoliva.wearable.navigation.Navigator
import com.bastyoliva.wearable.navigation.Routes
import com.bastyoliva.wearable.settings.presentation.DialogState
import com.bastyoliva.wearable.settings.presentation.Event
import com.bastyoliva.wearable.settings.presentation.SettingsViewModel
import com.bastyoliva.wearable.settings.usecase.NavigateToUseCase
import com.bastyoliva.wearable.settings.usecase.OpenAppOnPhoneUseCase
import com.bastyoliva.wearable.settings.usecase.OpenLinkOnPhoneUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRemoteHandler(var succeed: Boolean = true) : RemoteInteractionHandler {
        var openAppCallCount = 0
        var openLinkCallCount = 0
        var lastLink = ""

        override suspend fun openAppOnPhone(): Result<Unit> {
            openAppCallCount++
            return if (succeed) Result.success(Unit) else Result.failure(Exception("Launch failed"))
        }

        override suspend fun openRemoteLink(url: String): Result<Unit> {
            openLinkCallCount++
            lastLink = url
            return if (succeed) Result.success(Unit) else Result.failure(Exception("Link failed"))
        }
    }

    private class FakeNavigator : Navigator {
        override val navigationActions: Flow<NavigationAction> = emptyFlow()
        var lastRoute: Routes? = null
        override fun navigate(route: Routes) {
            lastRoute = route
        }
        override fun navigateUp() {}
        override fun navigateAndClearBackStack(route: Routes, popupTo: Routes, inclusive: Boolean) {}
    }

    @Test
    fun testOpenPhoneAppSuccessFlow() = runTest {
        val fakeRemote = FakeRemoteHandler(succeed = true)
        val fakeNav = FakeNavigator()
        val navUseCase = NavigateToUseCase(fakeNav)
        val linkUseCase = OpenLinkOnPhoneUseCase(fakeRemote)
        val openAppUseCase = OpenAppOnPhoneUseCase(fakeRemote)

        val viewModel = SettingsViewModel(navUseCase, linkUseCase, openAppUseCase)
        assertEquals(DialogState.CLOSED, viewModel.dialogState)

        viewModel.onEvent(Event.OnOpenPhoneApp)
        assertEquals(DialogState.OPENING_PHONE, viewModel.dialogState)

        advanceUntilIdle()

        assertEquals(1, fakeRemote.openAppCallCount)
        assertEquals(DialogState.SUCCESS, viewModel.dialogState)

        viewModel.onEvent(Event.CloseDialog)
        assertEquals(DialogState.CLOSED, viewModel.dialogState)
    }

    @Test
    fun testOpenPhoneAppFailureFlow() = runTest {
        val fakeRemote = FakeRemoteHandler(succeed = false)
        val fakeNav = FakeNavigator()
        val navUseCase = NavigateToUseCase(fakeNav)
        val linkUseCase = OpenLinkOnPhoneUseCase(fakeRemote)
        val openAppUseCase = OpenAppOnPhoneUseCase(fakeRemote)

        val viewModel = SettingsViewModel(navUseCase, linkUseCase, openAppUseCase)

        viewModel.onEvent(Event.OnOpenPhoneApp)
        advanceUntilIdle()

        assertEquals(1, fakeRemote.openAppCallCount)
        assertEquals(DialogState.FAILED, viewModel.dialogState)
    }

    @Test
    fun testTurboBoostDialogState() {
        val fakeRemote = FakeRemoteHandler()
        val fakeNav = FakeNavigator()
        val viewModel = SettingsViewModel(
            NavigateToUseCase(fakeNav),
            OpenLinkOnPhoneUseCase(fakeRemote),
            OpenAppOnPhoneUseCase(fakeRemote)
        )

        viewModel.onEvent(Event.OnOpenTurboBoost)
        assertEquals(DialogState.TURBO_INFO, viewModel.dialogState)

        viewModel.onEvent(Event.CloseDialog)
        assertEquals(DialogState.CLOSED, viewModel.dialogState)
    }

    @Test
    fun testEmailAndDonationLinks() = runTest {
        val fakeRemote = FakeRemoteHandler()
        val fakeNav = FakeNavigator()
        val viewModel = SettingsViewModel(
            NavigateToUseCase(fakeNav),
            OpenLinkOnPhoneUseCase(fakeRemote),
            OpenAppOnPhoneUseCase(fakeRemote)
        )

        viewModel.onEvent(Event.OnOpenEmail)
        advanceUntilIdle()
        assertEquals("mailto:basty.oliva2011@gmail.com", fakeRemote.lastLink)

        viewModel.onEvent(Event.OnOpenDonate)
        advanceUntilIdle()
        assertTrue(fakeRemote.lastLink.contains("#donations"))
    }
}
