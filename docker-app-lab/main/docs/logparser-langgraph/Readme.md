To ingest external logs dynamically during container runtime:

log-analyzer/
├── app/
│   ├── graph.py              # LangGraph workflow
│   ├── gmail_agent.py        # Gmail API email parsing
│   ├── alert_agent.py        # Trigger alerts from logs/emails
│   ├── dashboard.py          # Streamlit front-end
│   └── utils.py              # Reusable parsing helpers
├── .env
├── Dockerfile
├── docker-compose.yml
└── requirements.txt


Via CLI Flag

python main.py --cli       # Runs CLI mode
python main.py             # Defaults to dashboard

Via Environment Variable

export MODE=cli
python main.py             # Runs CLI



log-analyzer/
├── app/
│   ├── alert_agent.py         # 🚨 Detect critical logs/emails and raise alerts
│   ├── cli_runner.py          # 🧑‍💻 CLI mode for quick analysis
│   ├── dashboard.py           # 📊 Streamlit dashboard UI
│   ├── gmail_agent_stub.py    # 📬 Simulated Gmail parser
│   ├── graph.py               # 🧠 LangGraph workflow builder
│   ├── log_watcher.py         # 📡 Real-time log tailing
│   ├── logger.py              # 📋 Shared logging layer
│   └── test_data/
│       ├── emails.json        # Sample email data
│       └── logs/
│           └── demo.log       # Log file to watch
├── main.py                    # 🔀 Unified entrypoint
├── requirements.txt           # 📦 Pinned Python dependencies
├── Dockerfile                 # 🐳 Multi-stage Docker build
├── docker-compose.yml         # ⚙️ Compose orchestration
└── .env                       # 🔐 Environment config

log-analyzer/
├── config/
│   └── settings.yaml        # 🧪 Dynamic config file
Build Docker Image

docker build -t log-analyzer .

Run Container (Default Streamlit mode)

docker run -p 8501:8501 --env-file .env log-analyzer

 Run in CLI Mod

docker run -e MODE=cli --env-file .env log-analyzer


Run with:
docker-compose up --build
