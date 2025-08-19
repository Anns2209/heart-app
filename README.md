# Heartbeat Classifier – Android & Server Project

This project is part of a bachelor thesis at FERI, University of Maribor.  
It consists of:
- **Android mobile application** (built in Android Studio, Java)  
- **Python server** (`serverr.py`) with a trained classifier model from [AutomaticHeartSoundClassification](https://github.com/SiyuLou/AutomaticHeartSoundClassification)  
- **Apache reverse proxy configuration**  
- **Docker setup** (for local development and testing)

The system allows the user to **record or upload a heartbeat audio signal (WAV)** and sends it to the server, where a **deep learning classifier** determines whether the heart sound is **normal** or **abnormal**. The server returns a JSON response (and optionally a mel-spectrogram image).

---

## Project Structure

heart-app/
├── klient/
│ └── HeartbeatClassifier/ # Android application 
├── streznik/
│ ├── apache/ # Apache configuration (for reverse proxy)
│ │ ├── httpd.conf
│ │ └── zz-heart-app.conf
│ ├── app/
│ │ ├── AutomaticHeartSoundClassification/ # classification model (PyTorch)
│ │ ├── serverr.py # main server 
│ │ ├── Dockerfile # Docker configuration za 
│ │ ├── find_threshold.py # script for dedicating the classification threshold
│ │ ├── requirements.txt 
│ │ ├── threshold.json 
│ │ ├── spectrograms/ # Genearted  Mel-spektrogram pictures
│ │ └── uploads/ # WAV files
│ └── docker-compose.yml # Docker startup for the entire system
├── README.md


---

## Requirements

- **Python 3.9+**
- **pip / virtualenv**
- **Apache2** with modules: `proxy`, `proxy_http`, `headers`
- **Docker & docker-compose** (optional, for local dev)
- **Android Studio (Arctic Fox or newer)**

---

## Installation & Setup

### 1. Production Setup (University VM – recommended)
The project is deployed at:
http://164.8.67.103/api/


Steps to set up on a new server (without Docker):

```bash
# Clone the project
git clone https://github.com/Anns2209/heart-app.git
cd heart-app/streznik/app

# Create venv and install dependencies
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt


# Run the server
python serverr.py

#if it is successfull you will get a message: Strežnik teče na portu 5012…

#test the server locally:
curl -X POST -F "file=@a0001.wav" http://127.0.0.1:5012/classify


#open android studio and start the application


###  Apache Reverse Proxy Configuration

The Apache configuration files (httpd.conf and zz-heart-app.conf) are included in the streznik/apache/ folder.
To enable the reverse proxy, follow these steps:
# Copy or edit the config file
sudo nano /etc/httpd/conf.d/zz-heart-app.conf

# Restart Apache service
sudo systemctl restart httpd

# (Optional) If Apache is not running:
sudo systemctl start httpd



### 2. Local deployment (with Docker)


For testing on localhost:

docker compose build
docker compose up -d

API will be available at:
http://localhost:5012/

{"status": "OK"}



### Android Application

- **Package:** `com.example.heartbeatclassifier`
- **Features:**
  - Record or select WAV audio
  - Upload to server (`/api/predict`)
  - Display classification result (normal / abnormal)
  - Show spectrogram image (if returned by API)

### Base URL Configuration
- **Production:** `http://164.8.67.103/api/`
- **Local Dev:** `http://10.0.2.2:5012/` (emulator → localhost)

The app uses `BuildConfig.BASE_URL` via Gradle product flavors.  
Simply switch between `devDebug` and `prodDebug` variants in Android Studio.

### Permissions (in `AndroidManifest.xml`)
```xml
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>




---

###  Model Training


- **Base repo:** [AutomaticHeartSoundClassification](https://github.com/SiyuLou/AutomaticHeartSoundClassification)
- **Input:** Log-Mel spectrograms
- **Model:** CRNN (3 input channels, 2 output classes)
- **Dataset:** `.wav` files + `label.csv`
- **Training script:** `train.py` (adapted)
- **Saved weights:** `model.pth` (copied into models/)

To retrain:
```bash
python train.py --config config_crnn.json


