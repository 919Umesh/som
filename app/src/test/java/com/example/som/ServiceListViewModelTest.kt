package com.example.som

import com.example.som.data.mock.MockSomApiService
import com.example.som.data.mock.MockFailureMode
import com.example.som.data.repository.ServiceRepository
import com.example.som.viewmodel.ServiceListState
import com.example.som.viewmodel.ServiceListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ServiceListViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoadPublishesSuccessState() = runTest(dispatcher) {
        val viewModel = ServiceListViewModel(
            ServiceRepository(MockSomApiService(latencyMillis = 0))
        )

        advanceUntilIdle()

        assertTrue(viewModel.state.value is ServiceListState.Success)
    }

    @Test
    fun initialLoadPublishesEmptyState() = runTest(dispatcher) {
        val viewModel = ServiceListViewModel(
            ServiceRepository(
                MockSomApiService(latencyMillis = 0, failureMode = MockFailureMode.EMPTY_SERVICES)
            )
        )

        advanceUntilIdle()

        assertTrue(viewModel.state.value is ServiceListState.Empty)
    }

    @Test
    fun initialLoadPublishesRecoverableErrorState() = runTest(dispatcher) {
        val viewModel = ServiceListViewModel(
            ServiceRepository(
                MockSomApiService(latencyMillis = 0, failureMode = MockFailureMode.SERVER_ERROR)
            )
        )

        advanceUntilIdle()

        assertTrue(viewModel.state.value is ServiceListState.Error)
    }
}
