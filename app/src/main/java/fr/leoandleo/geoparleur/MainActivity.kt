package fr.leoandleo.geoparleur

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import fr.leoandleo.geoparleur.domain.audio.MicAudioManager
import fr.leoandleo.geoparleur.ui.theme.GeoparleurTheme
import fr.leoandleo.geoparleur.ui.utils.getRecordAudioPermission
import kotlinx.coroutines.*


private const val REQUEST_RECORD_AUDIO_PERMISSION = 200

class MainActivity : ComponentActivity() {
    private var permissionToRecordAccepted = false
    private val mediaJob = Job()
    private val mediaScope = CoroutineScope(Dispatchers.Main + mediaJob)

    private var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeoparleurTheme {
                Greeting(mediaScope)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaScope.cancel()
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Greeting(mediaScope: CoroutineScope) {

    val permissionsState = rememberMultiplePermissionsState(
        permissions = getRecordAudioPermission()
    )
    var isPressed by remember {
        mutableStateOf(false)
    }
    val context = LocalContext.current

    var micAudioManager: MicAudioManager? = remember {
        null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        try {
                            isPressed = true
                            mediaScope.launch(Dispatchers.IO) {
                                if (!permissionsState.allPermissionsGranted)
                                    permissionsState.launchMultiplePermissionRequest()
                                else{
                                    if(micAudioManager == null){
                                        micAudioManager = MicAudioManager(context)
                                    }
                                    micAudioManager?.start()
                            }
                        }

                        awaitRelease()
                    } finally {
                        isPressed = false
                        micAudioManager?.stop()
                    }
            },
    )
}
) {


    Image(
        painter = painterResource(id = R.drawable.ic_speacker),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .align(Alignment.Center)
            .size(128.dp)
            .clip(CircleShape)
    )
}


}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    GeoparleurTheme {
        //Greeting()
    }
}

