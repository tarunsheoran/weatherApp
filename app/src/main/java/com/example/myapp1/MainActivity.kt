package com.example.myapp1
//f6e06814d5cff5427eb608520d9b9693

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ApiInterface
import com.example.WeatherApp
import com.example.myapp1.databinding.ActivityMainBinding
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

//        fetchWeatherData("Jaipur")
//        searchCity()
    }


    private fun searchCity() {
        val searchView = binding.searchView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "f6e06814d5cff5427eb608520d9b9693", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {

                val responseBody = response.body()
                if (response.isSuccessful && responseBody != null) {
                    Log.d("API_SUCCESS", "Weather data: ${response.body()}")
                    val temperature = responseBody.main.temp.toString()
                    val humidity = responseBody.main.humidity
                    val windSpeed = responseBody.wind.speed
                    val sunrise = responseBody.sys.sunrise.toLong()
                    val sunset = responseBody.sys.sunset.toLong()
                    val seaLevel = responseBody.main.pressure
                    val condition = responseBody.weather.firstOrNull()?.main ?: "Unknown"
                    val maxTemp = responseBody.main.temp_max
                    val minTemp = responseBody.main.temp_min

                    binding.temp.text = "$temperature °C"
                    binding.weather.text = condition
                    binding.maxTemp.text = "Max Temp: $maxTemp °C"
                    binding.minTemp.text = "Min Temp: $minTemp °C"
                    binding.humidity.text = "$humidity %"
                    binding.wind.text = "$windSpeed m/s"
                    binding.sunrise.text = time(sunrise)
                    binding.sunset.text = time(sunset)
                    binding.sea.text = "$seaLevel hPa"
                    binding.conditions.text = condition
                    binding.day.text = dayName(System.currentTimeMillis())
                    binding.date.text = date()
                    binding.cityName.text = cityName

                    changeImages(condition )
                }
                else {
                    Log.e("API_ERROR", "Response failed: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("API_ERROR", "Failed to fetch weather data: ${t.message}")

            }
        })
    }
    private fun changeImages(conditions: String) {
        when (conditions) {
            "Haze", "Partly Clouds", "Clouds", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.cloud_background)
                binding.lottieAnimationView.setAnimation(R.raw.cloud)
            }
            "Clear", "Clear Sky", "Sunny"-> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }
            "Rain", "Drizzle" -> {
                binding.root.setBackgroundResource(R.drawable.rain_background)
                binding.lottieAnimationView.setAnimation(R.raw.rain)
            }
            "Snow" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView.setAnimation(R.raw.snow)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.sunny_background)
                binding.lottieAnimationView.setAnimation(R.raw.sun)
            }

        }
        binding.lottieAnimationView.playAnimation()
    }

    private fun dayName(timeStamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date(timeStamp))
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
    private fun time(timeStamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timeStamp*1000))
    }
}