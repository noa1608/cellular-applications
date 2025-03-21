    package com.example.travel.viewmodel

    import android.util.Log
    import androidx.lifecycle.LiveData
    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.viewModelScope
    import androidx.lifecycle.liveData
    import com.example.travel.data.Post
    import com.example.travel.repository.PostRepository
    import kotlinx.coroutines.Dispatchers
    import kotlinx.coroutines.launch
    import androidx.lifecycle.MutableLiveData

    class PostViewModel(private val postRepository: PostRepository) : ViewModel() {
        private val _post = MutableLiveData<Post?>()
        private val _postUpdateResult = MutableLiveData<Boolean>()
        val postUpdateResult: LiveData<Boolean> get() = _postUpdateResult
        val post: LiveData<Post?> get() = _post
        private val _postInsertResult = MutableLiveData<Long>()
        val postInsertResult: LiveData<Long> get() = _postInsertResult
        fun insertPost(post: Post) {
            viewModelScope.launch(Dispatchers.IO) {
                val newPostId = postRepository.insertPost(post)
                _postInsertResult.postValue(newPostId.toLong())
            }
        }

        fun updatePost(post: Post) {
            viewModelScope.launch {
                try {
                    val result = postRepository.updatePost(post)
                    _postUpdateResult.postValue(result)
                } catch (e: Exception) {
                    _postUpdateResult.postValue(false)
                }
            }
        }


        fun deletePost(postId: Long) {
            viewModelScope.launch {
                postRepository.deletePostById(postId)
            }
        }

        val postList: LiveData<List<Post>> = postRepository.getAllPosts()


        fun getUserPosts(owner: String) = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
            emit(postRepository.getUserPosts(owner))
        }

        fun getPostById(postId: Long) {
            viewModelScope.launch {
                val result = postRepository.getPostById(postId)
                _post.value = result
            }
        }

    }
