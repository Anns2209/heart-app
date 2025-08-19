# Heartbeat Classifier – Android & Server Project

This project is part of a bachelor thesis at FERI, University of Maribor.  
It consists of:
- **Android mobile application** (built in Android Studio, Java)  
- **Python server** (`serverr.py`) with a trained classifier model from [AutomaticHeartSoundClassification](https://github.com/SiyuLou/AutomaticHeartSoundClassification)  
- **Apache reverse proxy configuration**  
- **Docker setup** (for local development and testing)

The system allows the user to **record or upload a heartbeat audio signal (WAV)** and sends it to the server, where a **deep learning classifier** determines whether the heart sound is **normal** or **abnormal**. The server returns a JSON response (and optionally a mel-spectrogram image).

---

## 🔹 Project Structure

├── android/ # Android Studio project (app source)
├── serverr.py # Python  server
├── model.pth # Trained PyTorch model
├── requirements.txt # Python dependencies
├── docker-compose.yml # Docker services for local dev
├── Dockerfile # API container build
├── apache/
│ └── heartbeat.conf # Apache virtual host & reverse proxy
└── README.md


---

## ⚙️ Requirements

- **Python 3.9+**
- **pip / virtualenv**
- **Apache2** with modules: `proxy`, `proxy_http`, `headers`
- **Docker & docker-compose** (optional, for local dev)
- **Android Studio (Arctic Fox or newer)**

---

## 🚀 Installation & Setup

### 1. Production Setup (University VM – recommended)
The project is deployed at:
http://164.8.67.103/api/


Steps to set up on a new server (without Docker):

```bash
# Clone the project
git clone <repo_url>
cd heart-app/streznik/app

# Create venv and install dependencies
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt

# Place the trained model
mkdir models
cp model.pth models/model.pth

# Run the server
python serverr.py



2. Local Development (with Docker)


For testing on localhost:

docker compose build
docker compose up -d

API will be available at:
http://localhost:5012/





 Model Training

Based on: AutomaticHeartSoundClassification
Input: Log-Mel spectrograms
Model: CRNN (3 input channels, 2 output classes)
Dataset: .wav files + label.csv
Training script: adapted from repository, saved as model.pth
To retrain:
python train.py --config config_crnn.json
Then copy the new model.pth into models/.


