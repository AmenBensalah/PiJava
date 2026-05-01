import urllib.request
import json

API_KEY = "AIzaSyBny7McZPfvUKJqlTNj2UW5H_kF5yaQB-w"
url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key={API_KEY}"
data = {
    "contents": [{"parts": [{"text": "Hello"}]}]
}

req = urllib.request.Request(url, json.dumps(data).encode("utf-8"), headers={"Content-Type": "application/json"})
try:
    with urllib.request.urlopen(req) as f:
        res = f.read()
        print(res.decode("utf-8"))
except urllib.error.HTTPError as e:
    print(e.code)
    print(e.read().decode("utf-8"))
