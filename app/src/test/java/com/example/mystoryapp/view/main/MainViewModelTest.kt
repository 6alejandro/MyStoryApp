package com.example.mystoryapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.mystoryapp.DataDummy
import com.example.mystoryapp.LiveDataTestUtil.getOrAwaitValue
import com.example.mystoryapp.MainDispatcherRule
import com.example.mystoryapp.data.StoriesPagingSource
import com.example.mystoryapp.data.UserRepository
import com.example.mystoryapp.data.pref.UserModel
import com.example.mystoryapp.data.response.ListStoryItem
import com.example.mystoryapp.view.StoriesAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var userRepository: UserRepository
    private val dummyStories = DataDummy.generateDummyStoryResponse()
    private val dummyToken =
        "6eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1c2VyLW54a2JsMGItOF9EdEZWYVQiLCJpYXQiOjE2OTkxNjkwMzV9.B_Tow5UBK9QvDvYZPe26Iyv9gEBWkflSys5PRwpvEXc"

//    @Before
//    fun setUp() {
//        Mockito.`when`(userRepository.getSession())
//            .thenReturn(flowOf(UserModel("dummy@email.com", dummyToken, true)))
//    }

    @Test
    fun `when Get Story Should Not Null and Return Data`() = runTest {
        val data: PagingData<ListStoryItem> = com.example.mystoryapp.view.main.StoriesPagingSource.snapshot(dummyStories)
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data

        Mockito.`when`(userRepository.getStories(dummyToken)).thenReturn(expectedStory)

        val mainViewModel = MainViewModel(userRepository)
        val actualStory: PagingData<ListStoryItem> = mainViewModel.getStories(dummyToken).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStories.size, differ.snapshot().size)
        assertEquals(dummyStories[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Story Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data
        Mockito.`when`(userRepository.getStories(dummyToken)).thenReturn(expectedStory)
        val mainViewModel = MainViewModel(userRepository)
        val actualStory: PagingData<ListStoryItem> = mainViewModel.getStories(dummyToken).getOrAwaitValue()
        val differ = AsyncPagingDataDiffer(
            diffCallback = StoriesAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualStory)

        assertEquals(0, differ.snapshot().size)
    }
}

class StoriesPagingSource : PagingSource<Int, LiveData<List<ListStoryItem>>>() {
    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, LiveData<List<ListStoryItem>>>): Int? {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LiveData<List<ListStoryItem>>> {
        return LoadResult.Page(emptyList(), 0, 1)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {

    }

    override fun onRemoved(position: Int, count: Int) {

    }

    override fun onMoved(fromPosition: Int, toPosition: Int) {

    }

    override fun onChanged(position: Int, count: Int, payload: Any?) {

    }
}
