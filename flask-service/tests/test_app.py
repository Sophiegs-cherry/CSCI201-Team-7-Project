# flask-service/tests/test_app.py

"""
Flask AI Service Unit Tests
Covers Testing Plan sections 3.4-3.5 plus additional edge cases
"""

import pytest
import json
from app import app


@pytest.fixture
def client():
    """Create Flask test client"""
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client


class TestFlaskPlaylistGeneration:
    """Test suite for Flask playlist generation endpoint"""

    def test_generate_playlist_returns_valid_tracks(self, client, mocker):
        """
        Test 3.4 — Flask Service Returns Valid Tracks
        Type: Unit test (Flask)
        Input: POST /generate-playlist with mood, musicPreferences, context
        Expected: HTTP 200, JSON array with trackName, artistName, youtubeMusicUrl
        """
        # Arrange - Mock Gemini API response
        mock_gemini_songs = [
            {"title": "Calm Down", "artist": "Rema"},
            {"title": "Lofi Study Beats", "artist": "ChilledCow"},
            {"title": "Peaceful Piano", "artist": "Ludovico Einaudi"}
        ]

        class MockGeminiResponse:
            def __init__(self):
                self.text = json.dumps(mock_gemini_songs)

        # Mock YouTube Music search results
        def mock_ytmusic_search(query, filter=None, limit=None):
            if "Calm Down" in query:
                return [{"videoId": "mock_calm_down_id"}]
            elif "Lofi" in query:
                return [{"videoId": "mock_lofi_id"}]
            elif "Peaceful" in query:
                return [{"videoId": "mock_peaceful_id"}]
            return []

        # Apply mocks
        mocker.patch('app.client.models.generate_content', return_value=MockGeminiResponse())
        mocker.patch('app.ytmusic.search', side_effect=mock_ytmusic_search)

        # Act
        response = client.post('/generate-playlist',
            json={
                'mood': 'calm',
                'musicPreferences': 'lo-fi',
                'context': 'studying'
            },
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 200

        data = json.loads(response.data)
        assert isinstance(data, list), "Response should be a list"
        assert len(data) == 3, "Should return 3 validated tracks"

        # Verify each track has required fields with non-empty values
        for track in data:
            assert 'trackName' in track, "Track must have trackName"
            assert 'artistName' in track, "Track must have artistName"
            assert 'youtubeMusicUrl' in track, "Track must have youtubeMusicUrl"
            
            assert track['trackName'] != '', "trackName cannot be empty"
            assert track['artistName'] != '', "artistName cannot be empty"
            assert track['youtubeMusicUrl'] != '', "youtubeMusicUrl cannot be empty"
            
            # Verify YouTube Music URL format
            assert 'music.youtube.com/watch?v=' in track['youtubeMusicUrl'], \
                "URL must be valid YouTube Music link"

    def test_generate_playlist_empty_mood_returns_400(self, client):
        """
        Test 3.5 — Flask Service Handles Empty Mood
        Type: Unit test (Flask)
        Input: POST /generate-playlist with empty mood
        Expected: HTTP 400 Bad Request with error message
        """
        # Act
        response = client.post('/generate-playlist',
            json={
                'mood': '',
                'musicPreferences': 'rock',
                'context': 'workout'
            },
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 400, "Empty mood should return 400"
        
        data = json.loads(response.data)
        assert 'error' in data, "Response should contain error field"
        assert data['error'] == 'mood is required', "Error message should indicate mood is required"

    def test_generate_playlist_missing_mood_field(self, client):
        """Test missing mood field returns 400"""
        # Act
        response = client.post('/generate-playlist',
            json={
                'musicPreferences': 'jazz',
                'context': 'relaxing'
            },
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 400, "Missing mood field should return 400"
        
        data = json.loads(response.data)
        assert 'error' in data, "Response should contain error"

    def test_generate_playlist_mood_only(self, client, mocker):
        """Test mood-only request works (optional fields can be null/missing)"""
        # Arrange
        mock_gemini_songs = [
            {"title": "Happy", "artist": "Pharrell Williams"}
        ]

        class MockGeminiResponse:
            def __init__(self):
                self.text = json.dumps(mock_gemini_songs)

        def mock_ytmusic_search(query, filter=None, limit=None):
            return [{"videoId": "ZbZSe6N_BXs"}]

        mocker.patch('app.client.models.generate_content', return_value=MockGeminiResponse())
        mocker.patch('app.ytmusic.search', side_effect=mock_ytmusic_search)

        # Act
        response = client.post('/generate-playlist',
            json={'mood': 'happy'},
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 200
        
        data = json.loads(response.data)
        assert len(data) >= 1, "Should return at least 1 track"
        assert data[0]['trackName'] == 'Happy'
        assert data[0]['artistName'] == 'Pharrell Williams'

    def test_generate_playlist_gemini_error_returns_500(self, client, mocker):
        """Test graceful handling when Gemini API fails"""
        # Arrange - Mock Gemini to raise exception
        mocker.patch('app.client.models.generate_content', 
                    side_effect=Exception("Gemini API error"))

        # Act
        response = client.post('/generate-playlist',
            json={
                'mood': 'happy',
                'musicPreferences': 'pop',
                'context': 'party'
            },
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 500, "Gemini error should return 500"
        
        data = json.loads(response.data)
        assert 'error' in data, "Response should contain error"
        assert 'Gemini error' in data['error'], "Error should mention Gemini"

    def test_generate_playlist_skips_unfound_tracks(self, client, mocker):
        """Test handling when YouTube Music can't find a track"""
        # Arrange
        mock_gemini_songs = [
            {"title": "Nonexistent Song", "artist": "Fake Artist"},
            {"title": "Happy", "artist": "Pharrell Williams"}
        ]

        class MockGeminiResponse:
            def __init__(self):
                self.text = json.dumps(mock_gemini_songs)

        def mock_ytmusic_search(query, filter=None, limit=None):
            # Return empty for fake song, valid for real song
            if "Nonexistent" in query:
                return []
            return [{"videoId": "ZbZSe6N_BXs"}]

        mocker.patch('app.client.models.generate_content', return_value=MockGeminiResponse())
        mocker.patch('app.ytmusic.search', side_effect=mock_ytmusic_search)

        # Act
        response = client.post('/generate-playlist',
            json={'mood': 'happy'},
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 200
        
        data = json.loads(response.data)
        # Should only return the valid track (skipped the nonexistent one)
        assert len(data) == 1, "Should skip unfound tracks"
        assert data[0]['trackName'] == 'Happy'

    def test_generate_playlist_invalid_json_returns_400(self, client):
        """Test invalid JSON returns 400 or 415"""
        # Act
        response = client.post('/generate-playlist',
            data='invalid json {{{',
            content_type='application/json'
        )

        # Assert - Flask returns 400 for bad JSON
        assert response.status_code in [400, 415], "Invalid JSON should return 400 or 415"

    def test_generate_playlist_count_in_range(self, client, mocker):
        """Test that Flask returns 15-20 songs as specified in requirements"""
        # Arrange - Mock Gemini to return exactly 18 songs
        mock_songs = [
            {"title": f"Song {i}", "artist": f"Artist {i}"} 
            for i in range(1, 19)
        ]

        class MockGeminiResponse:
            def __init__(self):
                self.text = json.dumps(mock_songs)

        def mock_ytmusic_search(query, filter=None, limit=None):
            # Return valid result for all songs
            song_num = query.split()[1] if len(query.split()) > 1 else "1"
            return [{"videoId": f"video{song_num}"}]

        mocker.patch('app.client.models.generate_content', return_value=MockGeminiResponse())
        mocker.patch('app.ytmusic.search', side_effect=mock_ytmusic_search)

        # Act
        response = client.post('/generate-playlist',
            json={'mood': 'energetic'},
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 200
        
        data = json.loads(response.data)
        assert 15 <= len(data) <= 20, "Should return between 15-20 songs"
        assert len(data) == 18, "All 18 songs should be validated"

    def test_generate_playlist_strips_markdown_backticks(self, client, mocker):
        """Test that Flask handles Gemini responses with markdown formatting"""
        # Arrange - Gemini sometimes returns JSON wrapped in ```json ... ```
        mock_songs = [{"title": "Test Song", "artist": "Test Artist"}]
        markdown_response = f"```json\n{json.dumps(mock_songs)}\n```"

        class MockGeminiResponse:
            def __init__(self):
                self.text = markdown_response

        def mock_ytmusic_search(query, filter=None, limit=None):
            return [{"videoId": "test_id"}]

        mocker.patch('app.client.models.generate_content', return_value=MockGeminiResponse())
        mocker.patch('app.ytmusic.search', side_effect=mock_ytmusic_search)

        # Act
        response = client.post('/generate-playlist',
            json={'mood': 'calm'},
            content_type='application/json'
        )

        # Assert
        assert response.status_code == 200
        
        data = json.loads(response.data)
        assert len(data) == 1, "Should parse markdown-wrapped JSON"
        assert data[0]['trackName'] == 'Test Song'


# Run with: pytest tests/test_app.py -v