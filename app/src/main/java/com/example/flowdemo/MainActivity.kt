package com.example.flowdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.flowdemo.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Sceanrio 1: producer delay(1000L), consumer delay(2000L) when flow.collect{} starts executing it will run in same coroutine context that of collector is
 *  defined and produces following output, where producer and collector causes a overall delay of 3 secs
 *  Output:
 *  2021-04-04 12:34:11.778 D/MainActivity: Start flow
2021-04-04 12:34:12.787 D/MainActivity: Emitting 0
2021-04-04 12:34:12.797 D/MainActivity: 0
2021-04-04 12:34:15.804 D/MainActivity: Emitting 1
2021-04-04 12:34:15.804 D/MainActivity: 1
2021-04-04 12:34:18.810 D/MainActivity: Emitting 2
2021-04-04 12:34:18.811 D/MainActivity: 2
2021-04-04 12:34:21.818 D/MainActivity: Emitting 3
2021-04-04 12:34:21.818 D/MainActivity: 3
2021-04-04 12:34:24.824 D/MainActivity: Emitting 4
2021-04-04 12:34:24.824 D/MainActivity: 4
2021-04-04 12:34:27.830 D/MainActivity: Emitting 5
2021-04-04 12:34:27.830 D/MainActivity: 5

 * Scenario 2:  producer delay(1000L), consumer delay(2000L) when flow.buffer.collect{} starts executing it will run in different coroutine context that of collector is
 *  defined and produces following output, where producer will cause 1 sec delay but collector starts collecting only after 2 sec delay
 *  Output:
 *  2021-04-04 12:37:55.821 3834-3834/com.example.flowdemo D/MainActivity: Emitting 0
2021-04-04 12:37:55.830 3834-3834/com.example.flowdemo D/MainActivity: 0
2021-04-04 12:37:56.831 3834-3834/com.example.flowdemo D/MainActivity: Emitting 1
2021-04-04 12:37:57.833 3834-3834/com.example.flowdemo D/MainActivity: 1
2021-04-04 12:37:57.834 3834-3834/com.example.flowdemo D/MainActivity: Emitting 2
2021-04-04 12:37:58.837 3834-3834/com.example.flowdemo D/MainActivity: Emitting 3
2021-04-04 12:37:59.836 3834-3834/com.example.flowdemo D/MainActivity: 2
2021-04-04 12:37:59.839 3834-3834/com.example.flowdemo D/MainActivity: Emitting 4
2021-04-04 12:38:00.842 3834-3834/com.example.flowdemo D/MainActivity: Emitting 5
2021-04-04 12:38:01.839 3834-3834/com.example.flowdemo D/MainActivity: 3
2021-04-04 12:38:01.845 3834-3834/com.example.flowdemo D/MainActivity: Emitting 6
2021-04-04 12:38:02.848 3834-3834/com.example.flowdemo D/MainActivity: Emitting 7
2021-04-04 12:38:03.842 3834-3834/com.example.flowdemo D/MainActivity: 4
2021-04-04 12:38:03.850 3834-3834/com.example.flowdemo D/MainActivity: Emitting 8
2021-04-04 12:38:04.852 3834-3834/com.example.flowdemo D/MainActivity: Emitting 9
2021-04-04 12:38:05.844 3834-3834/com.example.flowdemo D/MainActivity: 5

Scenario 3: producer delay(1000L) has .flowOn(Dispatchers.Default), consumer delay(2000L)  starts executing it will run in different coroutine context that of collector is
 *  defined and produces following output, where producer will cause 1 sec delay but collector starts collecting only after 2 sec delay
 *  Output:
 *
 *  2021-04-04 12:41:45.472 4014-4045/com.example.flowdemo D/MainActivity: Emitting 0
2021-04-04 12:41:45.480 4014-4014/com.example.flowdemo D/MainActivity: 0
2021-04-04 12:41:46.482 4014-4045/com.example.flowdemo D/MainActivity: Emitting 1
2021-04-04 12:41:47.483 4014-4045/com.example.flowdemo D/MainActivity: Emitting 2
2021-04-04 12:41:47.484 4014-4014/com.example.flowdemo D/MainActivity: 1
2021-04-04 12:41:48.486 4014-4045/com.example.flowdemo D/MainActivity: Emitting 3
2021-04-04 12:41:49.487 4014-4014/com.example.flowdemo D/MainActivity: 2
2021-04-04 12:41:49.489 4014-4045/com.example.flowdemo D/MainActivity: Emitting 4
2021-04-04 12:41:50.491 4014-4045/com.example.flowdemo D/MainActivity: Emitting 5
2021-04-04 12:41:51.491 4014-4014/com.example.flowdemo D/MainActivity: 3
2021-04-04 12:41:51.494 4014-4045/com.example.flowdemo D/MainActivity: Emitting 6
2021-04-04 12:41:52.496 4014-4045/com.example.flowdemo D/MainActivity: Emitting 7
2021-04-04 12:41:53.495 4014-4014/com.example.flowdemo D/MainActivity: 4
2021-04-04 12:41:53.498 4014-4045/com.example.flowdemo D/MainActivity: Emitting 8
2021-04-04 12:41:54.501 4014-4045/com.example.flowdemo D/MainActivity: Emitting 9
2021-04-04 12:41:55.499 4014-4014/com.example.flowdemo D/MainActivity: 5
 */

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {

    lateinit var flow: Flow<Int>
    lateinit var fixedFlow: Flow<Int>
    lateinit var collectionFlow: Flow<Int>
    lateinit var channelFlow: Flow<Int>

    lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /**
         * Making login visibility login to show flow builders
         * For stateflow I have created login feature using vewimodel to use the `binding.textInputLayout.visibility == View.GONE` in else clause
         * to check stateflow with login
         */
        if (binding.textInputLayout.visibility == View.GONE) {
            setupLambdaFlow()
            setUpChannelFlowWithLambda()
            setupCollectionFlow()
            setupFlowOf()

            collectLambdaFlow()
            collectFlowOf()
            collectCollectionFlow()
            collectChannelFlow()

            binding.flowChainBtn.setOnClickListener {
                CoroutineScope(Dispatchers.Main).launch {
                    flowChain()
                }
            }
        } else {

            binding.textInputLayout.isVisible = true
            binding.textInputLayout2.isVisible = true
            binding.btnLogin.isVisible = true
            login()
        }
    }

    fun login() {

        binding.btnLogin.setOnClickListener {
            viewModel.login(
                binding.etUsername.text.toString(),
                binding.etPassword.text.toString()
            )
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loginUiState.collect {
                when (it) {
                    is MainViewModel.LoginUiState.Success -> {
                        Snackbar.make(
                            binding.root,
                            "Successfully logged in",
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.progressBar.isVisible = false
                    }
                    is MainViewModel.LoginUiState.Error -> {
                        Snackbar.make(
                            binding.root,
                            it.message,
                            Snackbar.LENGTH_LONG
                        ).show()
                        binding.progressBar.isVisible = false
                    }
                    is MainViewModel.LoginUiState.Loading -> {
                        binding.progressBar.isVisible = true
                    }
                    else -> Unit
                }
            }
        }

    }

    /**
     * flowchain to be in a  coroutine because collect() is a suspend function
     * Incase to make api calls inside flow{} we need to ensure we make them run in separate thread, since its in coroutine context
     * we have to use dispatchers and here it would be to use .flowOn(Dispatchers.IO)
     * By Default the coroutine context of producer is same for .collect() as well which preserves the context
     */
    suspend fun flowChain() {

        flow {
            Log.d(TAG, "from producer ${Thread.currentThread().name}")
            (0..10).forEach {
                // Emit items with 500 milliseconds delay
                Log.d(TAG, "Emitting  $it")
                emit(it)
            }
        }.flowOn(Dispatchers.IO) // the code above the producer runs in a worker thread and
            .collect {
                Log.d(
                    TAG,
                    "collect flow chain ${Thread.currentThread().name}"
                ) // this will run in dispatchers.main called where initially this is been called.
                Log.d(TAG, "from chain $it")
            }
    }


    /**
     * Lambda flow builder
     */
    fun setupLambdaFlow() {
        flow = flow {
            Log.d(TAG, "Start flow")
            (0..10).forEach {
                // Emit items with 500 milliseconds delay
                Log.d(TAG, "Emitting $it")
                emit(it)
            }
        }
    }

    /**
     * flowOf builder
     */
    fun setupFlowOf() {
        fixedFlow = flowOf(1, 2, 3, 4).onEach {
            Log.d(TAG, "EmittingFixedFlow $it")
            delay(300)
        }

    }

    /**
     * convert to flow from collection asFlow() builder
     */
    fun setupCollectionFlow() {
        val list = listOf(1, 2, 3, 5, 6, 7)
        collectionFlow = list.asFlow().onEach {
            Log.d(TAG, "EmittingCollectionFlow $it")
            delay(300)
        }


    }

    /**
     * Channel flow builder
     */
    fun setUpChannelFlowWithLambda() {
        channelFlow = channelFlow {
            (0..5).forEach {
                // Emit items with 500 milliseconds delay
                Log.d(TAG, "EmittingChannelFlow $it")
                send(it)
            }
        }
    }

    fun collectLambdaFlow() {
        binding.lamdaBtn.isVisible = true
        binding.lamdaBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {

                flow.collect {
                    Log.d(TAG, it.toString())
                    delay(2000)
                }
            }
        }
    }

    fun collectFlowOf() {
        binding.flowOfBtn.isVisible = true
        binding.flowOfBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                fixedFlow.collect {
                    Log.d(TAG, it.toString())
                    delay(2000)
                }
            }
        }
    }

    fun collectChannelFlow() {
        binding.channelflowBtn.isVisible = true
        binding.channelflowBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                channelFlow.collect {
                    Log.d(TAG, it.toString())
                    delay(2000)
                }
            }
        }
    }

    fun collectCollectionFlow() {
        binding.collectionflowBtn.isVisible = true
        binding.collectionflowBtn.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                collectionFlow.collect {
                    Log.d(TAG, it.toString())
                    delay(2000)
                }
            }
        }
    }


}