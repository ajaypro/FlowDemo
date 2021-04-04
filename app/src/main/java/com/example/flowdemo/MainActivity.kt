package com.example.flowdemo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.flowdemo.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

class MainActivity : AppCompatActivity() {

    lateinit var flow: Flow<Int>
    lateinit var fixedFlow: Flow<Int>
    lateinit var collectionFlow: Flow<Int>
    lateinit var channelFlow: Flow<Int>

    lateinit var binding: ActivityMainBinding

    val TAG = "MainActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupLambdaFlow()
        setUpChannelFlowWithLambda()
        setupCollectionFlow()
        setupFlowOf()

        collectLambdaFlow()
        collectFlowOf()
        collectCollectionFlow()
        collectChannelFlow()

    }

    /**
     * Lambda flow builder
     */
    fun setupLambdaFlow(){
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
    fun setupFlowOf(){
        fixedFlow = flowOf( 1,2,3,4).onEach {
            Log.d(TAG, "EmittingFixedFlow $it")
            delay(300) }

    }

    /**
     * convert to flow from collection asFlow() builder
     */
    fun setupCollectionFlow() {
        val list = listOf(1,2,3,5,6,7)
        collectionFlow = list.asFlow().onEach {
            Log.d(TAG, "EmittingCollectionFlow $it")
            delay(300) }



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