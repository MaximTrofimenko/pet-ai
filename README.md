# Running Pace Calculator API

A simple REST service that calculates running pace (minutes per kilometer) based on duration and distance.

## Overview

This service takes two inputs:
- Duration in HH:MM:SS or MM:SS format
- Distance in meters

And returns the pace in minutes per kilometer (MM:SS/km).

## Endpoints

### GET /
Returns information about the API and available endpoints.

### POST /calculate_pace
Calculates the pace based on the provided duration and distance.

#### Request Body
```json
{
  "duration": "HH:MM:SS or MM:SS",
  "distance": number (in meters)
}
```

#### Response
```json
{
  "duration": "input duration",
  "distance_meters": "input distance in meters",
  "pace": "calculated pace in MM:SS/km format"
}
```

## Examples

### Example 1: 30 minutes for 5000 meters
```bash
curl -X POST http://localhost:5000/calculate_pace \
  -H "Content-Type: application/json" \
  -d '{"duration": "00:30:00", "distance": 5000}'
```

Response:
```json
{
  "distance_meters": 5000.0,
  "duration": "00:30:00",
  "pace": "6:00/km"
}
```

### Example 2: 1 hour 15 minutes 30 seconds for 10000 meters
```bash
curl -X POST http://localhost:5000/calculate_pace \
  -H "Content-Type: application/json" \
  -d '{"duration": "01:15:30", "distance": 10000}'
```

Response:
```json
{
  "distance_meters": 10000.0,
  "duration": "01:15:30",
  "pace": "7:33/km"
}
```

## Setup and Run

1. Install dependencies:
```bash
pip install -r requirements.txt
```

2. Run the application:
```bash
python app.py
```

The service will be available at http://localhost:5000
