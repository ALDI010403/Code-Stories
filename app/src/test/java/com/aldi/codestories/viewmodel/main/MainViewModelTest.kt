package com.aldi.codestories.viewmodel.main

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import com.aldi.codestories.adapter.StoryAdapter
import com.aldi.codestories.repository.Result
import com.aldi.codestories.data.local.pref.UserPreference
import com.aldi.codestories.repository.StoryRepository
import com.aldi.codestories.response.ListStoryItem
import com.aldi.codestories.utils.DataDummy
import com.aldi.codestories.utils.MainDispatcherRule
import com.aldi.codestories.utils.StoryPagingSource
import com.aldi.codestories.utils.getOrAwaitValue
import com.aldi.codestories.utils.noopListUpdateCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock
    private lateinit var repository: StoryRepository
    @Mock
    private lateinit var preference: UserPreference
    private lateinit var mainViewModel: MainViewModel

    private lateinit var logMock: MockedStatic<Log>

    @Before
    fun setUp() {
        logMock = Mockito.mockStatic(Log::class.java)
        logMock.`when`<Boolean> { Log.isLoggable(Mockito.anyString(), Mockito.anyInt()) }.thenReturn(true)

        mainViewModel = MainViewModel(repository, preference)
    }

    @After
    fun tearDown() {
        logMock.close()
    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val dummyQuote = DataDummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyQuote)

        val expectedStory = flowOf(Result.Success(data))

        Mockito.`when`(repository.getAllStories()).thenReturn(expectedStory)

        val actualStory = mainViewModel.stories.getOrAwaitValue()
        val response = (actualStory as Result.Success).data

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(response)

        assertNotNull(differ.snapshot())
        assertEquals(dummyQuote.size, differ.snapshot().size)
        assertEquals(dummyQuote[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())

        val expectedStory = flowOf(Result.Success(data))

        Mockito.`when`(repository.getAllStories()).thenReturn(expectedStory)

        val actualStory = mainViewModel.stories.getOrAwaitValue()
        val response = (actualStory as Result.Success).data

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(response)

        assertEquals(0, differ.snapshot().size)
    }
}