import os
from collections import defaultdict
from google.auth.transport.requests import Request
from google.oauth2.credentials import Credentials
from google_auth_oauthlib.flow import InstalledAppFlow
from googleapiclient.discovery import build

SCOPES = [
    'https://www.googleapis.com/auth/gmail.readonly',
    'https://www.googleapis.com/auth/gmail.modify'
]


def authenticate():
    creds = None
    if os.path.exists("token.json"):
        creds = Credentials.from_authorized_user_file("token.json", SCOPES)
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            flow = InstalledAppFlow.from_client_secrets_file("credentials.json", SCOPES)
            creds = flow.run_local_server(port=0)
        with open("token.json", "w") as token:
            token.write(creds.to_json())
    return build("gmail", "v1", credentials=creds)
def get_or_create_label(service, label_name):
    labels = service.users().labels().list(userId='me').execute().get('labels', [])
    for label in labels:
        if label['name'].lower() == label_name.lower():
            return label['id']
    
    # Create label if not found
    label_body = {
        "name": label_name,
        "labelListVisibility": "labelShow",
        "messageListVisibility": "show"
    }
    label = service.users().labels().create(userId='me', body=label_body).execute()
    return label['id']

def label_emails_by_senders(service, senders, label_name, max_results=100):
    label_id = get_or_create_label(service, label_name)
    labeled_count = 0

    for sender in senders:
        query = f'from:{sender}'
        results = service.users().messages().list(userId='me', q=query, maxResults=max_results).execute()
        messages = results.get('messages', [])
        for msg in messages:
            service.users().messages().modify(
                userId='me',
                id=msg['id'],
                body={'addLabelIds': [label_id]}
            ).execute()
            labeled_count += 1

    return labeled_count
def delete_emails_by_sender(service, sender_email, max_results=100):
    results = service.users().messages().list(userId='me', maxResults=max_results, q=f'from:{sender_email}').execute()
    messages = results.get('messages', [])

    deleted_ids = []
    for msg in messages:
        msg_id = msg['id']
        service.users().messages().delete(userId='me', id=msg_id).execute()
        deleted_ids.append(msg_id)

    return deleted_ids

def get_email_counts(service, max_results=1000):
    results = service.users().messages().list(userId='me', maxResults=max_results).execute()
    messages = results.get('messages', [])
    counts = defaultdict(int)

    for msg in messages:
        msg_data = service.users().messages().get(userId='me', id=msg['id'], format='metadata', metadataHeaders=['From']).execute()
        for header in msg_data.get('payload', {}).get('headers', []):
            if header['name'] == 'From':
                counts[header['value']] += 1
    return dict(counts)