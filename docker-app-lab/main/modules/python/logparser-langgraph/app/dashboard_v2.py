import streamlit as st
import json
import logging
from datetime import datetime
from app.alert_agent import alert_node

# Logger setup
logger = logging.getLogger("dashboard")

# 📊 Sidebar Config
st.sidebar.header("🔧 Alert Configuration")

log_keywords = st.sidebar.multiselect(
    "Log Keywords", ["ERROR", "CRITICAL", "WARNING"], default=["ERROR"]
)

email_keywords = st.sidebar.multiselect(
    "Email Keywords", ["crash", "down", "failed"], default=["crash", "down"]
)

email_senders = st.sidebar.multiselect(
    "Email Senders", [
        "alerts@uob.com",
        "admin@finastra.com",
        "support@corebank.com"
    ],
    default=["alerts@uob.com", "admin@finastra.com"]
)

output_type = st.sidebar.selectbox("Alert Output", ["console", "file", "both"], index=0)

# 🧠 Dynamic config dictionary
config = {
    "log_keywords": log_keywords,
    "email_keywords": email_keywords,
    "email_senders": email_senders,
    "alert_output": output_type
}

# 📋 Dashboard Header
st.title("📊 LangGraph Log Analyzer")
st.caption("Last update: " + datetime.now().strftime("%Y-%m-%d %H:%M:%S"))

# 📬 Email Viewer
st.subheader("Emails")
try:
    with open("app/test_data/emails.json") as f:
        emails = json.load(f)
        for e in emails:
            st.markdown(f"**{e['subject']}** – {e['sender']}")
except Exception as e:
    logger.error(f"Failed to load emails: {e}")
    st.warning("⚠️ Could not load emails.")

# 📁 Log Viewer
st.subheader("Log Alerts")
try:
    with open("app/test_data/logs/demo.log") as f:
        lines = f.readlines()
        latest_logs = [line for line in lines if any(k in line for k in log_keywords)]
        for log in latest_logs:
            st.code(log.strip())
except Exception as e:
    logger.error(f"Failed to load logs: {e}")
    st.warning("⚠️ Could not load log data.")
    latest_logs = []

# 🚨 Trigger Alerts
st.subheader("🚨 Triggered Alerts")

try:
    state = {
        "latest_log": latest_logs[-1] if latest_logs else "",
        "emails": emails
    }
    result = alert_node(state, config)
    alerts = result.get("alerts", [])
except Exception as e:
    logger.error(f"Alert node failed: {e}")
    alerts = ["⚠️ Unable to generate alerts due to an internal error."]

for a in alerts:
    st.warning(a)