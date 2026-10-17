# Firstapp FastAPI server

## Start

```powershell
cd D:\pycharm_study\pythonProject1\study.py\firstapp
python -m pip install -r requirements.txt
$env:FIRSTAPP_JWT_SECRET = "replace-with-a-long-random-secret"
python -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Health check: `http://127.0.0.1:8000/api/health`

For a USB-connected Android phone, run on the computer:

```powershell
adb reverse tcp:8000 tcp:8000
```

The Android debug client then uses `http://127.0.0.1:8000/`. For a release build, use HTTPS and a real domain.
