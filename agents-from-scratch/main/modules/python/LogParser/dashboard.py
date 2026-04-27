import streamlit as st
from app.graph import build_graph

st.title("🔎 LangGraph Log Analyzer")

uploaded = st.file_uploader("Choose a log file")
if uploaded:
    lines = uploaded.read().decode("utf-8").splitlines()
    graph = build_graph()
    result = graph.invoke({"log_lines": lines})

    st.subheader("📊 Level Counts")
    st.bar_chart(result["level_counts"])

    st.subheader("❌ Errors")
    st.text("\n".join(result["errors"]))

    st.subheader("🚨 Anomalies")
    st.write(result["anomalies"])