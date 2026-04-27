import json

def gmail_stub_node(state, config=None):
    with open("app/test_data/emails.json", "r") as f:
        fake_emails = json.load(f)
    return {"emails": fake_emails}