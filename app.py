from flask import Flask, request, jsonify
import re

app = Flask(__name__)

def parse_time(time_str):
    """
    Parse time string in format HH:MM:SS or MM:SS
    Returns total seconds
    """
    # Check if the format is HH:MM:SS or MM:SS
    pattern = r'^(\d{1,2}):(\d{2}):(\d{2})$|^(\d{1,2}):(\d{2})$'
    match = re.match(pattern, time_str.replace(',', ':'))
    
    if not match:
        raise ValueError("Time format should be HH:MM:SS or MM:SS")
    
    groups = match.groups()
    if groups[0] is not None:  # Format HH:MM:SS
        hours = int(groups[0])
        minutes = int(groups[1])
        seconds = int(groups[2])
    else:  # Format MM:SS
        hours = 0
        minutes = int(groups[3])
        seconds = int(groups[4])
    
    return hours * 3600 + minutes * 60 + seconds

def calculate_pace(total_seconds, distance_meters):
    """
    Calculate pace in minutes per kilometer
    """
    if distance_meters <= 0:
        raise ValueError("Distance must be greater than 0")
    
    # Convert distance to kilometers
    distance_km = distance_meters / 1000
    
    # Calculate pace in seconds per km
    pace_seconds_per_km = total_seconds / distance_km
    
    # Convert to minutes:seconds per km
    minutes = int(pace_seconds_per_km // 60)
    seconds = int(pace_seconds_per_km % 60)
    
    return f"{minutes}:{seconds:02d}/km"

@app.route('/calculate_pace', methods=['POST'])
def calculate_pace_endpoint():
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No JSON data provided'}), 400
        
        # Validate required fields
        if 'duration' not in data or 'distance' not in data:
            return jsonify({'error': 'Missing required fields: duration and distance'}), 400
        
        duration_str = data['duration']
        distance_meters = float(data['distance'])
        
        # Parse duration
        total_seconds = parse_time(duration_str)
        
        # Calculate pace
        pace_result = calculate_pace(total_seconds, distance_meters)
        
        return jsonify({
            'duration': duration_str,
            'distance_meters': distance_meters,
            'pace': pace_result
        })
    
    except ValueError as e:
        return jsonify({'error': str(e)}), 400
    except Exception as e:
        return jsonify({'error': 'Invalid input format'}), 400

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        'message': 'Running Pace Calculator API',
        'endpoints': {
            'POST /calculate_pace': {
                'description': 'Calculate pace based on duration and distance',
                'input': {
                    'duration': 'Time in HH:MM:SS or MM:SS format',
                    'distance': 'Distance in meters (as number)'
                },
                'output': {
                    'pace': 'Pace in MM:SS/km format'
                }
            }
        }
    })

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)