package com.plcoding.videoplayercompose

import android.os.Bundle
import android.provider.SyncStateContract.Helpers.update
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.media3.ui.PlayerView
import com.plcoding.videoplayercompose.ui.theme.VideoPlayerComposeTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel as hiltViewModel
import androidx.lifecycle.LifecycleEventObserver as LifecycleEventObserver
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VideoPlayerComposeTheme {
                val viewModel = hiltViewModel<MainViewModel>()
                val videoItem by viewModel.videoItems.collectAsState()
                val selectVideoLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent(),
                    onResult = {
                        uri->
                        uri?.let(viewModel::addVideoUri)
                    }
                )
                var lifecycle by remember{
                    mutableStateOf(Lifecycle.Event.ON_CREATE)
                }
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner){

                        val observer= object : LifecycleEventObserver{
                            override fun onStateChanged(
                                source: LifecycleOwner,
                                event: Lifecycle.Event
                            ) {
                                lifecycle = event
                            }
                        }

                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ){
                    AndroidView(factory = { context -> 
                        PlayerView(context).also{
                            it.player = viewModel.player
                        }
                    },
                        update={
                              when(lifecycle) {
                                  Lifecycle.Event.ON_PAUSE->{
                                      it.onPause()
                                      it.player?.pause()
                                  }
                                  Lifecycle.Event.ON_RESUME->{
                                      it.onResume()
                                  }
                                  else-> Unit
                              }
                        },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)//todo: make this landscape

                        )
                    Spacer(modifier = Modifier.height(8.dp))
                    IconButton(onClick ={
                        selectVideoLauncher.launch("video/mp4")
                    }){
                        Icon(
                            imageVector= Icons.Default.FileOpen,
                            contentDescription = "Select video"
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth()
                    ){
                        items(videoItem){
                            item-> Text(
                                text = item.name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.playVideo(item.contentUri)
                                    }
                                    .padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}