package com.aldi.codestories.data.local.database

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.aldi.codestories.data.remote.ApiService
import com.aldi.codestories.response.ListStoryItem

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val storyDatabase: StoryDatabase,
    private val apiService: ApiService
) : RemoteMediator<Int, ListStoryItem>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ListStoryItem>
    ): MediatorResult {
        val page = getPageIndex(loadType, state) ?: return MediatorResult.Success(endOfPaginationReached = false)

        return try {
            val response = apiService.getStories(page, state.config.pageSize)
            val stories = response.listStory
            val endOfPaginationReached = stories.isEmpty()

            storyDatabase.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    clearDatabase()
                }
                val keys = stories.map {
                    RemoteKeys(it.id, getPrevKey(page), getNextKey(endOfPaginationReached, page))
                }
                storyDatabase.remoteKeysDao().insertAll(keys)
                storyDatabase.storyDao().insertStory(stories)
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    private suspend fun getPageIndex(loadType: LoadType, state: PagingState<Int, ListStoryItem>): Int? {
        return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_INDEX
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                remoteKeys?.prevKey ?: return null
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                remoteKeys?.nextKey ?: return null
            }
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.lastItemOrNull()?.let { item ->
            storyDatabase.remoteKeysDao().getRemoteKeysId(item.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.firstItemOrNull()?.let { item ->
            storyDatabase.remoteKeysDao().getRemoteKeysId(item.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, ListStoryItem>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                storyDatabase.remoteKeysDao().getRemoteKeysId(id)
            }
        }
    }

    private fun getPrevKey(page: Int): Int? {
        return if (page == 1) null else page - 1
    }

    private fun getNextKey(endOfPaginationReached: Boolean, page: Int): Int? {
        return if (endOfPaginationReached) null else page + 1
    }

    private suspend fun clearDatabase() {
        storyDatabase.remoteKeysDao().deleteRemoteKeys()
        storyDatabase.storyDao().deleteAll()
    }

    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}
