package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberImagePainter
import com.example.myapplication.ui.theme.MyApplicationTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "breedsList", Modifier.padding(16.dp)){
                    composable("breedsList") {
                        BreedsListScreen(navController)
                    }
                    composable("breedsImage/{breed}") { backStackEntry ->
                        val breed = backStackEntry.arguments?.getString("breed")?:""
                        BreedImageScreen(breed = breed, navController = navController)
                    }
                    composable("breedsImage/{breed}/{subBreed}") { backStackEntry ->
                        val breed = backStackEntry.arguments?.getString("breed")?:""
                        val subBreed = backStackEntry.arguments?.getString("subBreed")?:""
                        BreedImageScreen(breed,subBreed,navController)
                    }
                }
            }

        }
    }
}

val cachedImage: MutableMap<Pair<String, String?>, String> = mutableMapOf<Pair<String,String?>, String>()
@Composable
fun BreedImageScreen(breed: String, subBreed: String? = "" , navController: NavHostController) {

    var breedImage by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val key = Pair(breed,subBreed)
        breedImage = cachedImage[key]?:run {
            val image = if(subBreed.isNullOrEmpty()) service.getBreedImage(breed).message else service.getBreedImage(breed,subBreed).message
            cachedImage[key] = image
            image
        }
    }

    Column {
        Text(text = "$breed $subBreed")
        Image(painter = rememberImagePainter(breedImage), contentDescription = null)
    }
}

@Composable
fun BreedsListScreen(navController: NavHostController) {
    var breeds by remember { mutableStateOf((emptyMap <String, List<String>>())) }
    LaunchedEffect(Unit) {
        breeds = service.getBreedsList().message
    }

    LazyColumn {
        breeds.forEach{ (breed,subBreeds) ->
            item {
                Text(text = breed.uppercase(Locale.getDefault()),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.navigate("breedsImage/$breed") })
            }
            if(subBreeds.isNotEmpty()){
                items(subBreeds){subBreed->
                    Text(text = subBreed.lowercase(Locale.getDefault()),
                        modifier = Modifier.padding(start = 16.dp).clickable { navController.navigate("breedsImage/$breed/$subBreed") })
                }
            }
        }
    }
}


interface DogAPIService{
    @GET("breeds/list/all")
    suspend fun getBreedsList(): DogBreeds

    @GET("breed/{breed}/images/random")
    suspend fun getBreedImage(@Path("breed") breed: String): DogBreedImage

    @GET("breed/{breed}/{subBreed}/images/random")
    suspend fun getBreedImage(@Path("breed") breed: String, @Path("subBreed") subBreed: String): DogBreedImage


}

var retrofit = Retrofit.Builder()
    .baseUrl("https://dog.ceo/api/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

var service: DogAPIService = retrofit.create(DogAPIService::class.java)

data class DogBreeds (val message:Map<String, List<String>>)
data class DogBreedImage (val message:String)





