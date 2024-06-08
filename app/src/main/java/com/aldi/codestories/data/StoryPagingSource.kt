package com.aldi.codestories.data

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aldi.codestories.data.remote.ApiService
import com.aldi.codestories.response.ListStoryItem

class StoryPagingSource(private val apiService: ApiService) : PagingSource<Int, ListStoryItem>() {

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.let {
                it.prevKey?.plus(1) ?: it.nextKey?.minus(1)
            }
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        val position = params.key ?: INITIAL_PAGE_INDEX
        return try {
            val response = apiService.getStories(position, params.loadSize)
            val stories = response.listStory?.filterNotNull() ?: emptyList()
            logResponse(response, stories)
            LoadResult.Page(
                data = stories,
                prevKey = getPrevKey(position),
                nextKey = getNextKey(stories, position)
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    private fun getPrevKey(position: Int): Int? {
        return if (position == INITIAL_PAGE_INDEX) null else position - 1
    }

    private fun getNextKey(stories: List<ListStoryItem>, position: Int): Int? {
        return if (stories.isEmpty()) null else position + 1
    }

    private fun logResponse(response: Any, stories: List<ListStoryItem>) {
        Log.d("StoryPagingSource", "Response: $response")
        Log.d("StoryPagingSource", "Response Data: $stories")
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}
