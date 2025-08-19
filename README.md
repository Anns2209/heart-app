#  Heartbeat Classifier – Android & Server Project

This project is part of a bachelor thesis at FERI, University of Maribor.

It consists of:

- **Android mobile application** (built in Android Studio, Java)  
- **Python server** (`serverr.py`) with a trained classifier model from [AutomaticHeartSoundClassification](https://github.com/SiyuLou/AutomaticHeartSoundClassification)  
- **Apache reverse proxy configuration**  
- **Docker setup** (for local development and testing)

The system allows the user to **record or upload a heartbeat audio signal (WAV)** and sends it to the server, where a **deep learning classifier** determines whether the heart sound is **normal** or **abnormal**. The server returns a JSON response (and optionally a mel-spectrogram image).

---

##  Project Structure

heart-app/
├── klient/
│ └── HeartbeatClassifier/ # Android application
├── streznik/
│ ├── apache/ # Apache config (for reverse proxy)
│ │ ├── httpd.conf
│ │ └── zz-heart-app.conf
│ ├── app/
│ │ ├── AutomaticHeartSoundClassification/ # PyTorch model
│ │ ├── serverr.py # Main server
│ │ ├── Dockerfile # Docker setup
│ │ ├── find_threshold.py # Find classification threshold
│ │ ├── requirements.txt
│ │ ├── threshold.json
│ │ ├── spectrograms/ # Generated mel-spectrograms
│ │ └── uploads/ # Uploaded WAV files
│ └── docker-compose.yml
├── README.md


---

##  Requirements

- **Python 3.9+**
- **pip / virtualenv**
- **Apache2** with modules: `proxy`, `proxy_http`, `headers`
- **Docker & docker-compose** (optional for local testing)
- **Android Studio (Arctic Fox or newer)**

---

## Installation & Setup

###  1. Production Setup (University VM)

> The project is deployed at:  
> http://164.8.67.103/api/

Steps to set up the project **without Docker**:

```bash
# Clone the project
git clone https://github.com/Anns2209/heart-app.git
cd heart-app/streznik/app

# Create virtual environment
python3 -m venv venv
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Run the server
python serverr.py

You should see the message:
Strežnik teče na portu 5012…

To test it locally:

curl -X POST -F "file=@a0001.wav" http://127.0.0.1:5012/classify

Apache Reverse Proxy Configuration

Apache configuration files (httpd.conf, zz-heart-app.conf) are located in streznik/apache/.
On systems like Fedora or CentOS:

# Edit the reverse proxy config
sudo nano /etc/httpd/conf.d/zz-heart-app.conf

# Restart Apache
sudo systemctl restart httpd
# If Apache is not running yet:
sudo systemctl start httpd

Make sure these modules are loaded in httpd.conf:

LoadModule proxy_module modules/mod_proxy.so
LoadModule proxy_http_module modules/mod_proxy_http.so
LoadModule headers_module modules/mod_headers.so


2. Local Deployment (with Docker)

To run the whole system locally using Docker:
docker compose build
docker compose up -d

The API will be available at:
http://localhost:5012/
Test it:
curl -X POST -F "file=@a0001.wav" http://localhost:5012/classify



Android Application

Package: com.example.heartbeatclassifier
Features:
Record or select WAV audio
Upload to server (/api/predict)
Display classification result (normal / abnormal)
Show spectrogram image (if returned by server)

 Permissions
In AndroidManifest.xml:
<uses-permission android:name="android.permission.RECORD_AUDIO"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>



 Model Training

Based on: AutomaticHeartSoundClassification
Input: Log-Mel spectrograms
Model: CRNN (3 input channels, 2 output classes)
Dataset: .wav audio files + label.csv
Training script: train.py (adapted)
After training, the model is saved as model_best.pth and placed into saved/models/Pysionet_CRNN.

 To retrain:
python train.py --config config_crnn.json



