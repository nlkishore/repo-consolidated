import streamlit as st
import json
import logging
from app.alert_agent import alert_node
from app.config_loader import load_config

# Setup
logger = logging.getLogger("dashboard")
config = load_config("config/settings.yaml")

# Header
st.title("📊 LangGraph Log Analyzer")

# Show Emails
st.subheader("Emails")
with open("app/test_data/emails.json") as f:
    emails = json.load(f)
    for e in emails:
        st.markdown(f"**{e['subject']}** – {e['sender']}")

# Show Log Alerts
st.subheader("Log Alerts")
with open("app/test_data/logs/demo.log") as f:
    lines = f.readlines()
    latest = [line for line in lines if "ERROR" in line]
    for log in latest:
        st.code(log.strip())

# 🚨 Trigger Alerts Safely
st.subheader("🚨 Triggered Alerts")

try:
    state = {"latest_log": latest[-1] if latest else "", "emails": emails}
    result = alert_node(state, config)
    alerts = result.get("alerts", [])
except Exception as e:
    logger.error(f"Alert node failed: {e}")
    alerts = ["⚠️ Unable to generate alerts due to an internal error."]

for a in alerts:
    st.warning(a)