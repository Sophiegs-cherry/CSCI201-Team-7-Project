from flask import Flask, request, jsonify
from ytmusicapi import YTMusic
from google import genai
from dotenv import load_dotenv
import json
import os
from pathlib import Path

load_dotenv(dotenv_path=Path(__file__).with_name(".env"), override=True)

app = Flask(__name__)

client = genai.Client(api_key=os.environ.get("GEMINI_API_KEY"))
ytmusic = YTMusic()

@app.route("/generate-playlist", methods=["POST"])
def generate_playlist():
    data = request.json or {}

    mood = data.get("mood", "").strip()
    music_preferences = data.get("musicPreferences", "")
    context = data.get("context", "")

    # Return 400 if mood is empty (satisfies Test 3.5)
    if not mood:
        return jsonify({"error": "mood is required"}), 400

    # Build prompt
    prompt = f"The user is feeling: {mood}."
    if music_preferences:
        prompt += f" They like: {music_preferences}."
    if context:
        prompt += f" Additional context: {context}."
    prompt += """

Please recommend 15-20 songs that match this mood.
Return ONLY a JSON array with 'title' and 'artist' fields — no extra text, no markdown, no backticks.
Example:
[
  {"title": "song title", "artist": "artist name"}
]

Rules:
- Only real, existing, popular songs
- No made-up songs or artists
- No links, just names
"""

    try:
        response = client.models.generate_content(
            model="gemini-2.5-flash",
            contents=prompt
        )
    except Exception as e:
        return jsonify({"error": f"Gemini error: {str(e)}"}), 500

    # Strip markdown if Gemini adds it
    raw = response.text.strip()
    if raw.startswith("```"):
        raw = raw.split("```")[1]
        if raw.startswith("json"):
            raw = raw[4:]
    raw = raw.strip()

    try:
        songs = json.loads(raw)
    except json.JSONDecodeError as e:
        return jsonify({"error": f"Failed to parse Gemini response: {str(e)}"}), 500

    # Validate each track via ytmusicapi
    playlist = []
    for song in songs:
        title = song.get("title", "")
        artist = song.get("artist", "")

        if not title or not artist:
            continue

        try:
            results = ytmusic.search(f"{title} {artist}", filter="songs", limit=1)
            if results:
                video_id = results[0].get("videoId")
                if video_id:
                    playlist.append({
                        "trackName": title,
                        "artistName": artist,
                        "youtubeMusicUrl": f"https://music.youtube.com/watch?v={video_id}"
                    })
        except Exception:
            continue  # skip failed lookups, don't crash

    return jsonify(playlist)  # flat array, no wrapper

if __name__ == "__main__":
    app.run(port=5001, debug=True)
