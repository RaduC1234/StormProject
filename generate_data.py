import json
import random
import argparse
from datetime import datetime, timedelta

# Define possible weather conditions
CONDITIONS = ["SUNNY", "CLOUDY", "RAINY", "SNOWY", "UNKNOWN"]

# Function to generate fake locations
def generate_locations(num_locations):
    locations = []
    for i in range(1, num_locations + 1):
        location = {
            "id": i,
            "name": f"City_{i}",
            "latitude": round(random.uniform(-90.0, 90.0), 4),
            "longitude": round(random.uniform(-180.0, 180.0), 4)
        }
        locations.append(location)
    return locations

# Function to generate fake weather data for a given location
def generate_weather_data(locations, num_days):
    weather_data = []
    for location in locations:
        for day in range(num_days):
            date = datetime.today() - timedelta(days=day)
            weather = {
                "id": random.randint(1000, 9999),  # Random weather record ID
                "location": {
                    "id": location["id"],
                    "name": location["name"],
                    "latitude": location["latitude"],
                    "longitude": location["longitude"]
                },
                "date": date.strftime("%Y-%m-%d"),
                "maxTemperature": random.randint(0, 40),  # Max temp in Celsius
                "minTemperature": random.randint(-10, 25),  # Min temp in Celsius
                "condition": random.choice(CONDITIONS)
            }
            weather_data.append(weather)
    return weather_data

def main():
    # Command-line argument parsing
    parser = argparse.ArgumentParser(description="Generate fake location and weather data.")
    parser.add_argument("-l", "--locations", type=int, default=5, help="Number of locations to generate")
    parser.add_argument("-d", "--days", type=int, default=7, help="Number of days of weather data per location")
    parser.add_argument("-o", "--output", type=str, default="weather_data.json", help="Output JSON file name")
    
    args = parser.parse_args()

    # Generate data based on user input
    locations = generate_locations(args.locations)
    weather_data = generate_weather_data(locations, args.days)

    # Combine into a JSON structure
    data = {
        "locations": locations,
        "weather_records": weather_data
    }

    # Save to a JSON file
    with open(args.output, "w") as json_file:
        json.dump(data, json_file, indent=4)

    print(f"Fake weather and location data generated successfully! Check '{args.output}'.")

if __name__ == "__main__":
    main()
