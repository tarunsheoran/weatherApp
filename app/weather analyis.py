import requests
import sqlite3
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
from datetime import datetime
from sklearn.linear_model import LinearRegression

# Function to fetch current weather for a city
def fetch_weather_for_city(api_key):
    base_url = "http://api.openweathermap.org/data/2.5/weather"
    city = input("Enter the city name: ")
    params = {"q": city, "appid": api_key, "units": "metric"}
    response = requests.get(base_url, params=params)

    if response.status_code == 200:
        data = response.json()
        city_name = data.get("name")
        temperature = data["main"].get("temp")
        humidity = data["main"].get("humidity")
        pressure = data["main"].get("pressure", 1016)

        print(f"\nWeather details for {city_name}:")
        print(f"Temperature: {temperature}°C")
        print(f"Humidity: {humidity}%")
        print(f"Pressure: {pressure} hPa")

        # Generate predictions and visualizations
        predictions, accuracies = make_predictions(temperature, humidity, pressure)
        print(f"Accuracy: {accuracies['Next Day Accuracy']:.2f}%")
        save_predictions_to_db(city_name, predictions, accuracies, temperature)
        visualize_results(predictions, accuracies, city_name)
    else:
        print("City not found or invalid API request. Please try again!")

# Function to make predictions
def make_predictions(current_temp, humidity, pressure):
    historical_data = np.array([
        [60, 1010, 15.0], [65, 1012, 18.0], [70, 1013, 20.0],
        [75, 1014, 22.0], [67, 1016, 19.0], [63, 1015, 16.5],
        [62, 1011, 14.8], [70, 1018, 21.0], [72, 1019, 22.5],
        [68, 1017, 20.5]
    ])
    X = historical_data[:, :2]
    y = historical_data[:, 2]
    model = LinearRegression()
    model.fit(X, y)
    next_day_temp = model.predict([[humidity, pressure]])[0]
    day_after_temp = model.predict([[humidity + 2, pressure - 1]])[0]
    max_temp = max(current_temp, next_day_temp, day_after_temp) + 2
    min_temp = min(current_temp, next_day_temp, day_after_temp) - 2
    next_day_accuracy = 100 - abs((next_day_temp - current_temp) / current_temp) * 100
    predictions = {
        "Current Day": current_temp,
        "Next Day": next_day_temp,
        "Day After Tomorrow": day_after_temp,
        "Max Temp": max_temp,
        "Min Temp": min_temp,
        "Current Humidity": humidity,
        "Current Pressure": pressure,
    }
    accuracies = {"Next Day Accuracy": next_day_accuracy}
    return predictions, accuracies

# Function to save predictions to the database
def save_predictions_to_db(city_name, predictions, accuracies, actual_temp):
    conn = sqlite3.connect("weather_data.db")
    cursor = conn.cursor()
    cursor.execute("""
        CREATE TABLE IF NOT EXISTS weather_predictions (
            city TEXT,
            model TEXT,
            actual_temp REAL,
            predicted_temp REAL,
            accuracy REAL,
            timestamp TEXT
        )
    """)
    for key, value in predictions.items():
        cursor.execute("""
            INSERT INTO weather_predictions (city, model, actual_temp, predicted_temp, accuracy, timestamp)
            VALUES (?, ?, ?, ?, ?, ?)
        """, (city_name, key, actual_temp, value, accuracies.get("Next Day Accuracy", 0),
              datetime.now().strftime('%Y-%m-%d %H:%M:%S')))
    conn.commit()
    conn.close()
    print("Predictions saved successfully to the database.")

# Function to visualize results
def visualize_results(predictions, accuracies, city_name):
    days = ["Current Day", "Next Day", "Day After Tomorrow"]
    temps = [predictions["Current Day"], predictions["Next Day"], predictions["Day After Tomorrow"]]
    max_temp = predictions["Max Temp"]
    min_temp = predictions["Min Temp"]
    accuracy = accuracies["Next Day Accuracy"]
    humidity = predictions["Current Humidity"]
    pressure = predictions["Current Pressure"]

    # Line Chart
    plt.figure(figsize=(10, 6))
    plt.plot(days, temps, marker='o', label='Predicted Temps', color='blue')
    plt.axhline(y=max_temp, color='red', linestyle='--', label=f'Max Temp ({max_temp}°C)')
    plt.axhline(y=min_temp, color='green', linestyle='--', label=f'Min Temp ({min_temp}°C)')
    plt.xlabel("Days")
    plt.ylabel("Temperature (°C)")
    plt.title(f"Weather Predictions for {city_name}")
    plt.legend()
    plt.show()

    # Pie Chart
    plt.figure(figsize=(8, 8))
    plt.pie(
        [predictions["Current Day"], predictions["Next Day"], predictions["Day After Tomorrow"]],
        labels=["Current Day", "Next Day", "Day After Tomorrow"],
        autopct='%1.1f%%',
        startangle=90,
        colors=["skyblue", "lightgreen", "orange"]
    )
    plt.title("Temperature Predictions Distribution")
    plt.show()

    # Histogram for Humidity
    plt.figure(figsize=(8, 6))
    sns.histplot([humidity], bins=5, kde=True, color='purple')
    plt.xlabel("Humidity (%)")
    plt.ylabel("Frequency")
    plt.title("Humidity Distribution")
    plt.show()

    # Scatter Plot for Temperature vs. Humidity
    plt.figure(figsize=(8, 6))
    plt.scatter(humidity, temps[0], color='blue', label='Current Day')
    plt.scatter(humidity, temps[1], color='green', label='Next Day')
    plt.scatter(humidity, temps[2], color='orange', label='Day After Tomorrow')
    plt.xlabel("Humidity (%)")
    plt.ylabel("Temperature (°C)")
    plt.title("Temperature vs Humidity")
    plt.legend()
    plt.show()

# Main execution
if __name__ == "__main__":
    API_KEY = "f20cda5afb17ee1724ec97e1af6d7dd3"  # Replace with your actual API key
    fetch_weather_for_city(API_KEY)
