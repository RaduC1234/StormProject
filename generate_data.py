import json
import random
import argparse
from datetime import datetime, timedelta

CONDITIONS = ["SUNNY", "CLOUDY", "RAINY", "SNOWY", "UNKNOWN"]

def generate_locations(num_locations):
    locations = []
    for i in range(num_locations):
        location = {
            "name": f"City_{i + 1}",
            "latitude": round(random.uniform(-90.0, 90.0), 4),
            "longitude": round(random.uniform(-180.0, 180.0), 4)
        }
        locations.append(location)
    return locations

def determine_condition(max_temp):
    if max_temp < 5:
        return "SNOWY"
    elif 5 <= max_temp <= 15:
        return random.choice(["CLOUDY", "RAINY"])
    elif max_temp > 15:
        return random.choice(["SUNNY", "CLOUDY", "RAINY"])
    else:
        return "UNKNOWN"

def generate_weather_data(locations, num_days):
    weather_data = []
    for location in locations:
        for day in range(num_days):
            date = datetime.today() + timedelta(days=day)
            max_temp = random.randint(-10, 40)
            min_temp = random.randint(-20, max_temp - 1)
            condition = determine_condition(max_temp)
            weather = {
                "location": {
                    "name": location["name"],
                    "latitude": location["latitude"],
                    "longitude": location["longitude"]
                },
                "date": date.strftime("%Y-%m-%d"),
                "maxTemperature": max_temp,
                "minTemperature": min_temp,
                "condition": condition
            }
            weather_data.append(weather)
    return weather_data

def main():
    parser = argparse.ArgumentParser(description="Generate fake location and weather data.")
    parser.add_argument("-l", "--locations", type=int, default=5, help="Number of locations to generate")
    parser.add_argument("-d", "--days", type=int, default=7, help="Number of days of weather data per location")
    parser.add_argument("-o", "--output", type=str, default="weather_data.json", help="Output JSON file name")
    args = parser.parse_args()

    locations = generate_locations(args.locations)
    weather_data = generate_weather_data(locations, args.days)

    data = {
        "locations": locations,
        "weather_records": weather_data
    }

    with open(args.output, "w") as json_file:
        json.dump(data, json_file, indent=4)

    print(f"Fake weather and location data generated successfully! Check '{args.output}'.")

if __name__ == "__main__":
    main()
