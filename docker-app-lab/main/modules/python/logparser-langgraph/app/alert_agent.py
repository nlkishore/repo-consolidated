def alert_node(state, config):
    logs = state.get("latest_log", "")
    emails = state.get("emails", [])
    log_keywords = config.get("log_keywords", [])
    email_keywords = config.get("email_keywords", [])
    allowed_senders = config.get("email_senders", [])
    output_type = config.get("alert_output", "console")

    alerts = []

    # Log keyword matching
    for kw in log_keywords:
        if kw in logs:
            alerts.append(f"Log alert: {logs.strip()}")

    # Email keyword + sender matching
    for email in emails:
        if any(k in email["body"].lower() for k in email_keywords) and \
           email["sender"] in allowed_senders:
            alerts.append(f"Email alert: {email['subject']}")

    # Optional output routing
    if output_type in ["console", "both"]:
        for a in alerts:
            print(f"🚨 {a}")
    if output_type in ["file", "both"]:
        with open("app/test_data/alerts.log", "a") as f:
            for a in alerts:
                f.write(f"{a}\n")

    return {"alerts": alerts}